package com.management.task.management.service;

public record PasswordResetRequestedEvent(
        String email,
        String resetLink
) {
}
