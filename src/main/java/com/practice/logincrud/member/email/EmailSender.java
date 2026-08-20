package com.practice.logincrud.member.email;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;          // ✅ Spring 꺼
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class EmailSender {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendVerificationCode(String email, String code) {
        try {
            // ✅ HTML 파일 읽기
            ClassPathResource resource = new ClassPathResource("templates/email/verification-email.html");
            String html = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            // ✅ 플레이스홀더 치환
            String now = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            html = html.replace("{{code}}", code)
                    .replace("{{email}}", email)
                    .replace("{{datetime}}", now);

            // ✅ 메일 발송
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("[Admin Manager] 이메일 인증번호");
            helper.setText(html, true);
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
