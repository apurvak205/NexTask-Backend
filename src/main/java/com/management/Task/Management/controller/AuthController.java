package com.management.task.management.controller;

import com.management.task.management.dto.ForgetPasswordRequestDTO;
import com.management.task.management.dto.ResetPasswordRequestDTO;
import com.management.task.management.dto.UserLoginRequestDTO;
import com.management.task.management.dto.UserLoginResponseDTO;
import com.management.task.management.dto.UserRegisterRequestDTO;
import com.management.task.management.dto.UserRegisterResponseDTO;
import com.management.task.management.service.AuthService;
import com.management.task.management.service.PasswordResetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponseDTO> register(
            @Valid @RequestBody UserRegisterRequestDTO request) {
        authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new UserRegisterResponseDTO("User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDTO> login(
            @Valid @RequestBody @NotNull UserLoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid @RequestBody ForgetPasswordRequestDTO dto) {
        passwordResetService.createResetToken(dto);
        return ResponseEntity.ok("Reset link sent to email");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDTO dto) {
        passwordResetService.resetPassword(
                dto.getToken(),
                dto.getNewPassword()
        );
        return ResponseEntity.ok("Password reset successful");
    }
}
