package com.management.task.management.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PasswordResetMailListener {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetMailListener.class);

    private final MailService mailService;

    public PasswordResetMailListener(MailService mailService) {
        this.mailService = mailService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePasswordResetRequested(PasswordResetRequestedEvent event) {
        log.info("Attempting password reset email delivery to email={}", event.email());
        mailService.sendMail(
                event.email(),
                "Password Reset",
                "Reset your password using this link:\n" + event.resetLink()
        );
        log.info("Password reset email flow completed for email={}", event.email());
    }
}
