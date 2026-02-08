package com.management.task.management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

import javax.sql.DataSource;

@ConditionalOnBean(DataSource.class)
@Data
@AllArgsConstructor
public class UserRegisterResponseDTO {
    private String message;
}
