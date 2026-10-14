package com.questboard.dto.user;

import com.questboard.domain.enums.PlayerClass;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(max = 100) String displayName,
        @Size(max = 500) String avatarUrl,
        PlayerClass playerClass
) {}