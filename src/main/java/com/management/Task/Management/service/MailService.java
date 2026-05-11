package com.management.task.management.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final String apiKey;
    private final String fromEmail;

    public MailService(
            @Value("${SENDGRID_API_KEY}") String apiKey,
            @Value("${MAIL_FROM}") String fromEmail
    ) {
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
    }

    public void sendMail(String toEmail, String subject, String body) {
        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);
        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sendGrid = new SendGrid(apiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);
            int statusCode = response.getStatusCode();

            if (statusCode < 200 || statusCode >= 300) {
                log.error("SendGrid mail send failed for email={} status={} body={}", toEmail, statusCode, response.getBody());
                throw new IllegalStateException("Email delivery failed with status " + statusCode);
            }

            log.info("SendGrid mail sent successfully to email={} status={}", toEmail, statusCode);
        } catch (IOException ex) {
            log.error("SendGrid mail send failed for email={}", toEmail, ex);
            throw new IllegalStateException("Failed to send email", ex);
        }
    }
}
