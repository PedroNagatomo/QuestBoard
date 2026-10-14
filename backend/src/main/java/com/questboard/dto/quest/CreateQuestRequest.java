package com.questboard.dto.quest;

import com.questboard.domain.enums.Difficulty;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CreateQuestRequest(
        @NotBlank @Size(max = 255) String title,
        String description,
        Difficulty difficulty,
        UUID assigneeId,
        UUID guildId,
        UUID parentBossId,
        Instant dueDate,
        @Size(max = 20) List<@Size(max = 30) String> tags,
        @Min(0) @Max(100000) Integer xpOverride,
        @Min(0) @Max(100000) Integer coinOverride
) {}