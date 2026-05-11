package com.management.task.management.service;

import com.management.task.management.dto.UserLoginRequestDTO;
import com.management.task.management.dto.UserLoginResponseDTO;
import com.management.task.management.dto.UserRegisterRequestDTO;
import com.management.task.management.dto.UserResponseDTO;
import com.management.task.management.exception.BadRequestException;
import com.management.task.management.model.AuthProvider;
import com.management.task.management.model.User;
import com.management.task.management.repository.UserRepository;
import com.management.task.management.security.JwtService;
import com.management.task.management.util.PasswordValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public void register(UserRegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        PasswordValidationUtils.validate(request.getPassword());

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setProvider(AuthProvider.LOCAL);

        userRepository.save(user);
    }

    public UserLoginResponseDTO login(UserLoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (user.getProvider() == AuthProvider.GOOGLE) {
            throw new BadRequestException("Use Google login for this account");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails, user.getName());

        UserResponseDTO userDTO = new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail()
        );

        return new UserLoginResponseDTO(token, userDTO);
    }
}
