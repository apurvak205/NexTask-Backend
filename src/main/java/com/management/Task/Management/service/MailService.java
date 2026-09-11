package com.management.task.management.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MailService {

    private static final Logger log =
            LoggerFactory.getLogger(MailService.class);

    private static final String BREVO_API_URL =
            "https://api.brevo.com/v3/smtp/email";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String brevoApiKey;
    private final String fromEmail;

    public MailService(
            ObjectMapper objectMapper,
            @Value("${BREVO_API_KEY}") String brevoApiKey,
            @Value("${MAIL_FROM}") String fromEmail
    ) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
        this.brevoApiKey = brevoApiKey;
        this.fromEmail = fromEmail;
    }

    public void sendMail(String toEmail, String subject, String body) {

        try {
            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            Map<String, Object> sender = new HashMap<>();
            sender.put("name", "NexTask");
            sender.put("email", fromEmail);

            Map<String, Object> recipient = new HashMap<>();
            recipient.put("email", toEmail);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("sender", sender);
            requestBody.put("to", List.of(recipient));
            requestBody.put("subject", subject);
            requestBody.put("textContent", body);

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            BREVO_API_URL,
                            HttpMethod.POST,
                            request,
                            String.class
                    );

            log.info(
                    "Brevo email sent successfully to email={}, status={}",
                    toEmail,
                    response.getStatusCode()
            );

        } catch (Exception ex) {

            log.error(
                    "Brevo email sending failed for email={}",
                    toEmail,
                    ex
            );

            throw new IllegalStateException(
                    "Failed to send password reset email",
                    ex
            );
        }
    }
}