package com.management.task.management.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String appBaseUrl;
    private final String fromEmail;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${APP_BASE_URL:http://localhost:8181}") String appBaseUrl,
            @Value("${MAIL_FROM:}") String fromEmail
    ) {
        this.mailSender = mailSender;
        this.appBaseUrl = appBaseUrl;
        this.fromEmail = fromEmail;
    }

    public void sendResetEmail(String toEmail, String token) {
        String resetLink = appBaseUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        if (StringUtils.hasText(fromEmail)) {
            message.setFrom(fromEmail);
        }
        message.setSubject("Reset Your Password");
        message.setText(
                "You requested a password reset.\n\n" +
                        "Click below link:\n" + resetLink +
                        "\n\nThis link expires in 15 minutes."
        );

        try {
            mailSender.send(message);
        } catch (MailAuthenticationException ex) {
            throw new IllegalStateException("Email service authentication failed", ex);
        }
    }
}
