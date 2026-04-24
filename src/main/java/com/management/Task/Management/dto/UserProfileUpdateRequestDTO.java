package com.management.task.management.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateRequestDTO {

    private String name;

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 5, message = "Password must be at least 5 characters")
    private String password;
}
