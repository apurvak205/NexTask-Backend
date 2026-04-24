package com.management.task.management.controller;

import com.management.task.management.dto.UserLoginResponseDTO;
import com.management.task.management.dto.UserProfileUpdateRequestDTO;
import com.management.task.management.dto.UserRegisterResponseDTO;
import com.management.task.management.dto.UserResponseDTO;
import com.management.task.management.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @PutMapping("/me")
    public ResponseEntity<UserLoginResponseDTO> updateCurrentUser(
            @Valid @RequestBody UserProfileUpdateRequestDTO request) {
        return ResponseEntity.ok(userService.updateCurrentUser(request));
    }

    @DeleteMapping("/me")
    public ResponseEntity<UserRegisterResponseDTO> deleteCurrentUser() {
        userService.deleteCurrentUser();
        return ResponseEntity.ok(new UserRegisterResponseDTO("Profile deleted successfully"));
    }
}
