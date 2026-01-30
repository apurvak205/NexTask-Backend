package com.management.Task.Management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserLoginResponseDTO {
    private String token;
    private UserResponseDTO user;
}

