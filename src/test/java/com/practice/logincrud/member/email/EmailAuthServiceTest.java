package com.practice.logincrud.member.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * 이메일 인증코드 brute-force 방지(시도 횟수 제한) 검증.
 * 실제 메일 발송은 EmailSender를 mock으로 대체해서 SMTP/DB 없이 순수 로직만 검증한다.
 */
class EmailAuthServiceTest {

    private EmailAuthService emailAuthService;
    private EmailSender emailSender;
    private MockHttpSession session;
    private static final String EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        emailAuthService = new EmailAuthService();
        emailSender = mock(EmailSender.class);
        ReflectionTestUtils.setField(emailAuthService, "emailSender", emailSender);
        session = new MockHttpSession();
    }

    private String sendAndCaptureCode() {
        emailAuthService.sendCode(EMAIL, session);
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSender, atLeastOnce()).sendVerificationCode(anyString(), codeCaptor.capture());
        return codeCaptor.getAllValues().get(codeCaptor.getAllValues().size() - 1);
    }

    @Test
    void 올바른_코드는_한번에_인증된다() {
        String code = sendAndCaptureCode();

        boolean result = emailAuthService.verifyCode(EMAIL, code, session);

        assertThat(result).isTrue();
        assertThat(emailAuthService.isVerified(EMAIL, session)).isTrue();
    }

    @Test
    void 틀린_코드는_인증되지_않는다() {
        sendAndCaptureCode();

        boolean result = emailAuthService.verifyCode(EMAIL, "000000", session);

        assertThat(result).isFalse();
        assertThat(emailAuthService.isVerified(EMAIL, session)).isFalse();
    }

    @Test
    void 시도_횟수를_5번_초과하면_맞는_코드를_입력해도_인증에_실패한다() {
        String code = sendAndCaptureCode();
        String wrongCode = "000000".equals(code) ? "111111" : "000000";

        // 시도 횟수 제한(5회)을 다 소진할 때까지 일부러 틀린 코드로 시도
        for (int i = 0; i < 5; i++) {
            boolean attempt = emailAuthService.verifyCode(EMAIL, wrongCode, session);
            assertThat(attempt).isFalse();
        }

        // 6번째 시도는 실제로는 맞는 코드지만, 시도 횟수를 다 써서 코드가 이미 폐기된 상태여야 한다
        boolean finalAttempt = emailAuthService.verifyCode(EMAIL, code, session);

        assertThat(finalAttempt).isFalse();
        assertThat(emailAuthService.isVerified(EMAIL, session)).isFalse();
    }

    @Test
    void 만료된_코드는_인증되지_않는다() {
        String code = sendAndCaptureCode();
        // 만료 시각을 과거로 직접 조작해 5분 대기 없이 만료 상황을 재현
        session.setAttribute("EMAIL_EXPIRE_" + EMAIL, System.currentTimeMillis() - 1000);

        boolean result = emailAuthService.verifyCode(EMAIL, code, session);

        assertThat(result).isFalse();
    }

    @Test
    void 새_코드를_발송하면_이전_시도_횟수가_초기화된다() {
        String firstCode = sendAndCaptureCode();
        for (int i = 0; i < 5; i++) {
            emailAuthService.verifyCode(EMAIL, "000000".equals(firstCode) ? "111111" : "000000", session);
        }

        String secondCode = sendAndCaptureCode();
        boolean result = emailAuthService.verifyCode(EMAIL, secondCode, session);

        assertThat(result).isTrue();
    }
}
