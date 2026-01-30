package com.management.Task.Management.controller;

import com.management.Task.Management.dto.*;
import com.management.Task.Management.model.User;
import com.management.Task.Management.repository.UserRepository;
import com.management.Task.Management.security.JwtService;
import com.management.Task.Management.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponseDTO> register(
            @Valid @RequestBody UserRegisterRequestDTO request) {

        authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new UserRegisterResponseDTO("User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody @NotNull UserLoginRequestDTO request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(
                org.springframework.security.core.userdetails.User
                        .withUsername(user.getEmail())
                        .password(user.getPassword())
                        .roles("USER")
                        .build(),
                user.getName()
        );

        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", user.getName() // "Sai Tarun"
        ));
    }

}
