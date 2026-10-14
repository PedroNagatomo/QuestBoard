package com.questboard.dto.auth;

import com.questboard.domain.enums.PlayerClass;
import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "username deve conter apenas letras, números, _ . -")
        String username,

        @NotBlank @Email
        String email,

        @NotBlank @Size(min = 8, max = 100)
        String password,

        @Size(max = 100)
        String displayName,

        PlayerClass playerClass
) {}