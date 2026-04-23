package com.management.task.management.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgetPasswordRequestDTO {

    @NotBlank
    @Email
    private String email;

}
