package com.management.task.management.service;

import com.management.task.management.dto.*;
import com.management.task.management.model.User;
import com.management.task.management.repository.UserRepository;
import com.management.task.management.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    // ================= REGISTER =================
    public void register(UserRegisterRequestDTO request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));

        userRepository.save(user);
    }

    // ================= LOGIN =================
    public UserLoginResponseDTO login(UserLoginRequestDTO request) {

        // 1️⃣ Authenticate credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2️⃣ Fetch user from DB
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3️⃣ Load UserDetails (Spring Security way)
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(user.getEmail());

        // 4️⃣ Generate JWT token
        String token = jwtService.generateToken(userDetails, user.getName());

        // 5️⃣ Prepare response DTO
        UserResponseDTO userDTO = new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail()
        );

        return new UserLoginResponseDTO(token, userDTO);
    }
}
