package com.questboard.dto.quest;

import com.questboard.domain.Quest;
import com.questboard.domain.enums.Difficulty;
import com.questboard.domain.enums.QuestStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record QuestResponse(
        UUID id,
        String title,
        String description,
        Difficulty difficulty,
        QuestStatus status,
        int xpReward,
        int coinReward,
        Instant dueDate,
        Instant completedAt,
        UUID assigneeId,
        String assigneeUsername,
        UUID createdById,
        UUID guildId,
        UUID parentBossId,
        String externalSource,
        String externalUrl,
        List<String> tags,
        Instant createdAt
) {
    public static QuestResponse from(Quest q) {
        return new QuestResponse(
                q.getId(), q.getTitle(), q.getDescription(), q.getDifficulty(), q.getStatus(),
                q.getXpReward(), q.getCoinReward(), q.getDueDate(), q.getCompletedAt(),
                q.getAssignee() != null ? q.getAssignee().getId() : null,
                q.getAssignee() != null ? q.getAssignee().getUsername() : null,
                q.getCreatedBy() != null ? q.getCreatedBy().getId() : null,
                q.getGuild() != null ? q.getGuild().getId() : null,
                q.getParentBoss() != null ? q.getParentBoss().getId() : null,
                q.getExternalSource(), q.getExternalUrl(),
                q.getTags(), q.getCreatedAt()
        );
    }
}