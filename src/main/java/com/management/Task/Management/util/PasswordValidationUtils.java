package com.management.task.management.util;

import com.management.task.management.exception.BadRequestException;

public final class PasswordValidationUtils {

    public static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*\\d).{8,}$";
    public static final String PASSWORD_RULE_MESSAGE =
            "Password must be 8+ characters long and include at least 1 uppercase letter and 1 number";

    private PasswordValidationUtils() {
    }

    public static void validate(String password) {
        if (password == null || !password.matches(PASSWORD_REGEX)) {
            throw new BadRequestException(PASSWORD_RULE_MESSAGE);
        }
    }
}
