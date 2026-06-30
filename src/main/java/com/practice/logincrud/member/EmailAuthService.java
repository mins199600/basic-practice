package com.practice.logincrud.member;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class EmailAuthService {

    private static final String CODE_PREFIX = "EMAIL_CODE_";
    private static final String VERIFIED_PREFIX = "EMAIL_VERIFIED_";
    private static final String EXPIRE_PREFIX = "EMAIL_EXPIRE_";
    private static final long EXPIRE_MILLIS = 5 * 60 * 1000; // 5분

    @Autowired
    private JavaMailSender mailSender;

    public void sendCode(String email, HttpSession session) throws MessagingException {
        String code = createCode();

        session.setAttribute(CODE_PREFIX + email, code);
        session.setAttribute(EXPIRE_PREFIX + email, System.currentTimeMillis() + EXPIRE_MILLIS);
        session.setAttribute(VERIFIED_PREFIX + email, false);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
        helper.setTo(email);
        helper.setSubject("[LOGIN-CRUD] 이메일 인증번호");
        helper.setText("인증번호는 [" + code + "] 입니다. 5분 내 입력해주세요.");
        mailSender.send(message);
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
