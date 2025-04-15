package org.buddyguard.bodyguard.service;

import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.buddyguard.bodyguard.entity.User;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class MailService {

    private JavaMailSender mailSender;

    // 가입 환영 메일 전송 (HTML 형식)
    public boolean sendWelcomeHtmlMessage(User user) {

        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, "utf-8");

            // 수신자 이메일 설정
            messageHelper.setTo(user.getEmail());
            // 메일 제목 설정
            messageHelper.setSubject("[BuddyGuard] 보디가드 회원가입을 환영합니다.");
            // HTML 형식의 메일 본문 작성
            String html = "<h2>보디가드에 오신 것을 진심으로 환영합니다!</h2>";
            html += "<p>안녕하세요, " + user.getNickname() + "님!</p>";
            html += "<a href='http://localhost:8080'>보디가드</a>에 가입해주셔서 감사합니다";
            html += "<p>앞으로 다양한 컨텐츠와 서비스를 제공해 드리겠습니다.</p>";
            html += "<br/><br/>";
            html += "<p>팀 BuddyGuard 올림</p>";

            messageHelper.setText(html, true);

            mailSender.send(message);

        } catch (Exception e) {
            log.error("error = {}", e);

            return false;
        }

        return true;
    }


    // 임시 비밀번호 메일 전송
    public boolean sendTemporalPasswordMessage(String email, String temporalPassword) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("[BuddyGuard] 임시 비밀번호를 발급하였습니다.");
        message.setText("임시 비밀번호는 " + temporalPassword + "입니다.");

        boolean result = true;

        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.error("error = {}", e);
            result = false;
        }

        return result;
    }
}
