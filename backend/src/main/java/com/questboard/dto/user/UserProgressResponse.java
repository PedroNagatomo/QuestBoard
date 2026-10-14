package com.questboard.dto.user;

import java.util.List;
import java.util.UUID;

public record UserProgressResponse(
        UUID userId,
        int level,
        long xp,
        long xpToNext,
        double progressPercent,
        long totalXp,
        long coins,
        int currentStreak,
        int longestStreak,
        long questsCompleted,
        long bossesDefeated,
        List<BadgeView> badges,
        List<AchievementView> achievements
) {
    public record BadgeView(String code, String name, String iconUrl, String rarity) {}
    public record AchievementView(String code, String name, long progress, long target, boolean completed) {}
}