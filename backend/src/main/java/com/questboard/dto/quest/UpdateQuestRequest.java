package com.questboard.dto.quest;

import com.questboard.domain.enums.Difficulty;
import com.questboard.domain.enums.QuestStatus;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UpdateQuestRequest(
        @Size(max = 255) String title,
        String description,
        Difficulty difficulty,
        QuestStatus status,
        UUID assigneeId,
        Instant dueDate,
        List<String> tags
) {}