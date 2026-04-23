package com.management.task.management.service;

import com.management.task.management.dto.ForgetPasswordRequestDTO;
import com.management.task.management.exception.BadRequestException;
import com.management.task.management.exception.ResourceNotFoundException;
import com.management.task.management.model.AuthProvider;
import com.management.task.management.model.PasswordResetToken;
import com.management.task.management.model.User;
import com.management.task.management.repository.PasswordResetTokenRepository;
import com.management.task.management.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
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

        emailService.sendResetEmail(user.getEmail(), token);
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

        if (newPassword.length() < 6) {
            throw new BadRequestException("Password too short");
        }

        User user = resetToken.getUser();
        if (user.getProvider() == AuthProvider.GOOGLE) {
            throw new BadRequestException("Password reset is not available for Google login");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken);
    }
}
