package com.practice.logincrud.member;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class EmailAuthService {

    private static final String CODE_PREFIX = "EMAIL_CODE_";
    private static final String VERIFIED_PREFIX = "EMAIL_VERIFIED_";
    private static final String EXPIRE_PREFIX = "EMAIL_EXPIRE_";
    private static final long EXPIRE_MILLIS = 5 * 60 * 1000; // 5분

    @Autowired
    private EmailSender emailSender;

    // 인증코드를 세션에 즉시 기록하고, SMTP 발송(느림)은 EmailSender가 별도 스레드에서 처리하도록 넘겨
    // 컨트롤러가 메일 전송 완료를 기다리지 않고 바로 응답할 수 있게 한다.
    public void sendCode(String email, HttpSession session) {
        String code = createCode();

        session.setAttribute(CODE_PREFIX + email, code);
        session.setAttribute(EXPIRE_PREFIX + email, System.currentTimeMillis() + EXPIRE_MILLIS);
        session.setAttribute(VERIFIED_PREFIX + email, false);

        emailSender.sendVerificationCode(email, code);
    }

    public boolean verifyCode(String email, String inputCode, HttpSession session) {
        Object savedCodeObj = session.getAttribute(CODE_PREFIX + email);
        Object expireObj = session.getAttribute(EXPIRE_PREFIX + email);

        if (savedCodeObj == null || expireObj == null) return false;

        String savedCode = String.valueOf(savedCodeObj);
        long expireTime = (long) expireObj;

        if (System.currentTimeMillis() > expireTime) {
            clear(email, session);
            return false;
        }

        boolean match = savedCode.equals(inputCode);
        if (match) {
            session.setAttribute(VERIFIED_PREFIX + email, true);
            // 코드 재사용 방지
            session.removeAttribute(CODE_PREFIX + email);
            session.removeAttribute(EXPIRE_PREFIX + email);
        }
        return match;
    }

    public boolean isVerified(String email, HttpSession session) {
        Object verified = session.getAttribute(VERIFIED_PREFIX + email);
        return verified instanceof Boolean && (Boolean) verified;
    }

    public void clear(String email, HttpSession session) {
        session.removeAttribute(CODE_PREFIX + email);
        session.removeAttribute(EXPIRE_PREFIX + email);
        session.removeAttribute(VERIFIED_PREFIX + email);
    }

    private String createCode() {
        SecureRandom random = new SecureRandom();
        int n = 100000 + random.nextInt(900000); // 6자리
        return String.valueOf(n);
    }
}
