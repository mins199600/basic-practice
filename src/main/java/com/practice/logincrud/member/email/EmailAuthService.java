package com.practice.logincrud.member.email;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class EmailAuthService {

    private static final String CODE_PREFIX = "EMAIL_CODE_";
    private static final String VERIFIED_PREFIX = "EMAIL_VERIFIED_";
    private static final String EXPIRE_PREFIX = "EMAIL_EXPIRE_";
    private static final String ATTEMPTS_PREFIX = "EMAIL_ATTEMPTS_";
    private static final long EXPIRE_MILLIS = 5 * 60 * 1000; // 5분
    // 6자리 코드를 5분 동안 무제한으로 시도하면 브루트포스로 뚫릴 수 있어 시도 횟수를 제한한다.
    private static final int MAX_ATTEMPTS = 5;

    @Autowired
    private EmailSender emailSender;

    // 인증코드를 세션에 즉시 기록하고, SMTP 발송(느림)은 EmailSender가 별도 스레드에서 처리하도록 넘겨
    // 컨트롤러가 메일 전송 완료를 기다리지 않고 바로 응답할 수 있게 한다.
    public void sendCode(String email, HttpSession session) {
        String code = createCode();

        session.setAttribute(CODE_PREFIX + email, code);
        session.setAttribute(EXPIRE_PREFIX + email, System.currentTimeMillis() + EXPIRE_MILLIS);
        session.setAttribute(VERIFIED_PREFIX + email, false);
        session.setAttribute(ATTEMPTS_PREFIX + email, 0);

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

        int attempts = getAttempts(email, session);
        if (attempts >= MAX_ATTEMPTS) {
            // 시도 횟수를 다 써버렸으면 코드를 즉시 폐기 - 남은 시간 동안 더 시도해도 항상 실패한다.
            clear(email, session);
            return false;
        }

        boolean match = savedCode.equals(inputCode);
        if (match) {
            session.setAttribute(VERIFIED_PREFIX + email, true);
            // 코드 재사용 방지
            session.removeAttribute(CODE_PREFIX + email);
            session.removeAttribute(EXPIRE_PREFIX + email);
            session.removeAttribute(ATTEMPTS_PREFIX + email);
        } else {
            session.setAttribute(ATTEMPTS_PREFIX + email, attempts + 1);
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
        session.removeAttribute(ATTEMPTS_PREFIX + email);
    }

    private int getAttempts(String email, HttpSession session) {
        Object attempts = session.getAttribute(ATTEMPTS_PREFIX + email);
        return attempts instanceof Integer ? (Integer) attempts : 0;
    }

    private String createCode() {
        SecureRandom random = new SecureRandom();
        int n = 100000 + random.nextInt(900000); // 6자리
        return String.valueOf(n);
    }
}
