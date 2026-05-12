package com.management.Task.Management.service;

import com.management.task.management.dto.ForgetPasswordRequestDTO;
import com.management.task.management.exception.BadRequestException;
import com.management.task.management.model.AuthProvider;
import com.management.task.management.model.PasswordResetToken;
import com.management.task.management.model.User;
import com.management.task.management.repository.PasswordResetTokenRepository;
import com.management.task.management.repository.UserRepository;
import com.management.task.management.service.PasswordResetRequestedEvent;
import com.management.task.management.service.PasswordResetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<PasswordResetToken> tokenCaptor;

    @Captor
    private ArgumentCaptor<PasswordResetRequestedEvent> eventCaptor;

    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetService(
                userRepository,
                tokenRepository,
                passwordEncoder,
                eventPublisher,
                "http://localhost:3000/",
                "reset-password"
        );
    }

    @Test
    void createResetTokenSavesTokenAndQueuesEmail() {
        ForgetPasswordRequestDTO request = new ForgetPasswordRequestDTO();
        request.setEmail("user@example.com");

        User user = new User();
        user.setEmail("user@example.com");
        user.setProvider(AuthProvider.LOCAL);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        passwordResetService.createResetToken(request);

        verify(tokenRepository).deleteByUser(user);
        verify(tokenRepository).save(tokenCaptor.capture());
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        PasswordResetToken savedToken = tokenCaptor.getValue();
        PasswordResetRequestedEvent event = eventCaptor.getValue();

        assertThat(savedToken.getUser()).isEqualTo(user);
        assertThat(savedToken.getToken()).isNotBlank();
        assertThat(savedToken.getExpiryDate()).isNotNull();
        assertThat(event.email()).isEqualTo("user@example.com");
        assertThat(event.resetLink()).startsWith("http://localhost:3000/reset-password?token=");
        assertThat(event.resetLink()).endsWith(savedToken.getToken());
    }

    @Test
    void createResetTokenRejectsGoogleUsers() {
        ForgetPasswordRequestDTO request = new ForgetPasswordRequestDTO();
        request.setEmail("google@example.com");

        User user = new User();
        user.setEmail("google@example.com");
        user.setProvider(AuthProvider.GOOGLE);

        when(userRepository.findByEmail("google@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> passwordResetService.createResetToken(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Password reset is not available for Google login");
    }
}
