package com.management.task.management.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.management.task.management.util.PasswordValidationUtils;

@Data
public class UserProfileUpdateRequestDTO {

    private String name;

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 8, message = PasswordValidationUtils.PASSWORD_RULE_MESSAGE)
    @Pattern(regexp = PasswordValidationUtils.PASSWORD_REGEX, message = PasswordValidationUtils.PASSWORD_RULE_MESSAGE)
    private String password;
}
