package com.questboard.dto.user;

import com.questboard.domain.User;
import com.questboard.domain.enums.PlayerClass;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String displayName,
        String avatarUrl,
        PlayerClass playerClass,
        int level,
        long xp,
        long xpToNextLevel,
        long totalXp,
        long coins,
        int currentStreak,
        int longestStreak,
        LocalDate lastActiveDate,
        UUID guildId,
        String guildName,
        Instant createdAt
) {
    public static UserResponse from(User u, long xpToNext) {
        return new UserResponse(
                u.getId(), u.getUsername(), u.getEmail(), u.getDisplayName(), u.getAvatarUrl(),
                u.getPlayerClass(), u.getLevel(), u.getXp(), xpToNext, u.getTotalXp(), u.getCoins(),
                u.getCurrentStreak(), u.getLongestStreak(), u.getLastActiveDate(),
                u.getGuild() != null ? u.getGuild().getId() : null,
                u.getGuild() != null ? u.getGuild().getName() : null,
                u.getCreatedAt()
        );
    }
}