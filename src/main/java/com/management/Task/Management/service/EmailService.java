package com.management.task.management.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String frontendBaseUrl;
    private final String resetPath;
    private final String fromEmail;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${APP_FRONTEND_URL:http://localhost:3000}") String frontendBaseUrl,
            @Value("${PASSWORD_RESET_PATH:/reset-password}") String resetPath,
            @Value("${MAIL_FROM:}") String fromEmail
    ) {
        this.mailSender = mailSender;
        this.frontendBaseUrl = frontendBaseUrl;
        this.resetPath = resetPath;
        this.fromEmail = fromEmail;
    }

    public void sendResetEmail(String toEmail, String token) {
        String resetLink = buildResetLink(token);
        log.info("Preparing password reset email to email={} using from={}", toEmail, StringUtils.hasText(fromEmail) ? fromEmail : "<default>");

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
            log.info("Sending password reset email to email={}", toEmail);
            mailSender.send(message);
            log.info("Password reset email sent successfully to email={}", toEmail);
        } catch (MailAuthenticationException ex) {
            log.error("Mail authentication failed for email={}", toEmail, ex);
            throw new IllegalStateException("Email service authentication failed", ex);
        } catch (MailException ex) {
            log.error("Mail delivery failed for email={}", toEmail, ex);
            throw new IllegalStateException(buildMailFailureMessage(ex), ex);
        }
    }

    private String buildResetLink(String token) {
        String normalizedBaseUrl = frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                : frontendBaseUrl;
        String normalizedResetPath = resetPath.startsWith("/") ? resetPath : "/" + resetPath;
        return normalizedBaseUrl + normalizedResetPath + "?token=" + token;
    }

    private String buildMailFailureMessage(MailException ex) {
        Throwable rootCause = ex.getMostSpecificCause();
        String rootMessage = rootCause != null ? rootCause.getMessage() : ex.getMessage();
        if (StringUtils.hasText(rootMessage)) {
            return "Email delivery failed: " + rootMessage;
        }
        return "Email delivery failed";
    }
}
