package com.management.task.management.service;

import com.management.task.management.dto.ForgetPasswordRequestDTO;
import com.management.task.management.exception.BadRequestException;
import com.management.task.management.exception.ResourceNotFoundException;
import com.management.task.management.model.AuthProvider;
import com.management.task.management.model.PasswordResetToken;
import com.management.task.management.model.User;
import com.management.task.management.repository.PasswordResetTokenRepository;
import com.management.task.management.repository.UserRepository;
import com.management.task.management.util.PasswordValidationUtils;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final String frontendBaseUrl;
    private final String resetPath;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            ApplicationEventPublisher eventPublisher,
            @Value("${APP_FRONTEND_URL:http://localhost:3000}") String frontendBaseUrl,
            @Value("${PASSWORD_RESET_PATH:/reset-password}") String resetPath
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
        this.frontendBaseUrl = frontendBaseUrl;
        this.resetPath = resetPath;
    }

    @Transactional
    public void createResetToken(ForgetPasswordRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getProvider() == AuthProvider.GOOGLE) {
            throw new BadRequestException("Password reset is not available for Google login");
        }

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));

        tokenRepository.deleteByUser(user);
        tokenRepository.save(resetToken);
        log.info("Password reset token saved for email={}", user.getEmail());

        String resetLink = buildResetLink(token);
        eventPublisher.publishEvent(new PasswordResetRequestedEvent(user.getEmail(), resetLink));
        log.info("Password reset email queued for email={}", user.getEmail());
    }

    public PasswordResetToken validateToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Token expired");
        }

        return resetToken;
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = validateToken(token);
        PasswordValidationUtils.validate(newPassword);

        User user = resetToken.getUser();
        if (user.getProvider() == AuthProvider.GOOGLE) {
            throw new BadRequestException("Password reset is not available for Google login");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken);
    }

    private String buildResetLink(String token) {
        String normalizedBaseUrl = frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                : frontendBaseUrl;
        String normalizedResetPath = resetPath.startsWith("/") ? resetPath : "/" + resetPath;
        return normalizedBaseUrl + normalizedResetPath + "?token=" + token;
    }
}
