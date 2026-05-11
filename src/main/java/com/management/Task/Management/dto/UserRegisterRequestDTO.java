package com.management.task.management.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.management.task.management.util.PasswordValidationUtils;

@Data
public class UserRegisterRequestDTO {

    @NotBlank(message = "Full name is required")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = PasswordValidationUtils.PASSWORD_RULE_MESSAGE)
    @Pattern(regexp = PasswordValidationUtils.PASSWORD_REGEX, message = PasswordValidationUtils.PASSWORD_RULE_MESSAGE)
    private String password;
}

