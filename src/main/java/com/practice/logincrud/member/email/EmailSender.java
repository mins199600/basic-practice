package com.practice.logincrud.member.email;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

// SMTP 전송 전담 - 별도 빈으로 분리한 이유: @Async는 프록시를 통해서만 동작하는데,
// 같은 클래스 안에서 자기 자신의 @Async 메서드를 호출하면(self-invocation) 프록시를 안 거쳐서
// 비동기가 적용되지 않는다. EmailAuthService가 이 빈을 주입받아 호출해야 실제로 비동기로 돈다.
@Component
@Slf4j
public class EmailSender {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendVerificationCode(String email, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(email);
            helper.setSubject("이메일 인증번호");
            helper.setText("인증번호는 [" + code + "] 입니다. 5분 내 입력해주세요.");
            mailSender.send(message);
        } catch (Exception e) {
            log.error("인증코드 메일 비동기 발송 실패 email={}", email, e);
        }
    }

    @Async
    public void sendAdminApprovalRequest(String superAdminEmail, String requesterEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(superAdminEmail);
            helper.setSubject("[관리자 승인 요청] 신규 관리자 가입 신청이 도착했습니다");
            helper.setText("관리자 계정(" + requesterEmail + ")이 가입을 신청했습니다.\n"
                    + "관리자 대시보드의 승인 목록에서 확인 후 승인/거부해주세요.");
            mailSender.send(message);
        } catch (Exception e) {
            log.error("관리자 승인 요청 메일 비동기 발송 실패 superAdminEmail={}, requesterEmail={}", superAdminEmail, requesterEmail, e);
        }
    }
}
