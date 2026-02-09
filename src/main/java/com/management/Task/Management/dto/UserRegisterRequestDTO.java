package com.management.task.management.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

import javax.sql.DataSource;

@Data
public class UserRegisterRequestDTO {

    @NotBlank(message = "Full name is required")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @Size(min = 5, message = "Password must be at least 5 characters")
    @NotBlank(message = "Password is required")
    private String password;
}

