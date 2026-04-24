package com.management.task.management.service;

import com.management.task.management.dto.UserLoginResponseDTO;
import com.management.task.management.dto.UserProfileUpdateRequestDTO;
import com.management.task.management.dto.UserResponseDTO;
import com.management.task.management.exception.BadRequestException;
import com.management.task.management.exception.ResourceNotFoundException;
import com.management.task.management.model.AuthProvider;
import com.management.task.management.model.User;
import com.management.task.management.repository.PasswordResetTokenRepository;
import com.management.task.management.repository.TaskRepository;
import com.management.task.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final com.management.task.management.security.JwtService jwtService;

    public UserResponseDTO getCurrentUser() {
        User user = getAuthenticatedUser();
        return toUserResponse(user);
    }

    @Transactional
    public UserLoginResponseDTO updateCurrentUser(UserProfileUpdateRequestDTO request) {
        User user = getAuthenticatedUser();
        boolean hasUpdates = false;

        if (StringUtils.hasText(request.getName())) {
            user.setName(request.getName().trim());
            hasUpdates = true;
        }

        if (StringUtils.hasText(request.getEmail())) {
            if (user.getProvider() == AuthProvider.GOOGLE) {
                throw new BadRequestException("Google account email cannot be changed");
            }

            String newEmail = request.getEmail().trim();
            if (!user.getEmail().equalsIgnoreCase(newEmail)
                    && userRepository.existsByEmail(newEmail)) {
                throw new BadRequestException("Email already registered");
            }

            user.setEmail(newEmail);
            hasUpdates = true;
        }

        if (StringUtils.hasText(request.getPassword())) {
            if (user.getProvider() == AuthProvider.GOOGLE) {
                throw new BadRequestException("Google account password cannot be changed here");
            }

            user.setPassword(passwordEncoder.encode(request.getPassword()));
            hasUpdates = true;
        }

        if (!hasUpdates) {
            throw new BadRequestException("At least one field is required to update profile");
        }

        User savedUser = userRepository.save(user);
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String token = jwtService.generateToken(userDetails, savedUser.getName());

        return new UserLoginResponseDTO(token, toUserResponse(savedUser));
    }

    @Transactional
    public void deleteCurrentUser() {
        User user = getAuthenticatedUser();
        passwordResetTokenRepository.deleteByUser(user);
        taskRepository.deleteByUserId(user.getId());
        userRepository.delete(user);
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserResponseDTO toUserResponse(User user) {
        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail());
    }
}

