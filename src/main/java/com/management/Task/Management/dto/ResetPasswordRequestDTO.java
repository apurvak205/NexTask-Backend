package com.management.task.management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.management.task.management.util.PasswordValidationUtils;

@Data
public class ResetPasswordRequestDTO {

    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 8, message = PasswordValidationUtils.PASSWORD_RULE_MESSAGE)
    @Pattern(regexp = PasswordValidationUtils.PASSWORD_REGEX, message = PasswordValidationUtils.PASSWORD_RULE_MESSAGE)
    private String newPassword;

}
