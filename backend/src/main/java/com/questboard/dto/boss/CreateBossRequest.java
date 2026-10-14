package com.questboard.dto.boss;

import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.UUID;

public record CreateBossRequest(
        @NotBlank @Size(max = 150) String name,
        String description,
        @Size(max = 500) String imageUrl,
        @NotNull @Min(100) @Max(1_000_000) Long totalHp,
        UUID guildId,
        Instant startsAt,
        Instant endsAt
) {}