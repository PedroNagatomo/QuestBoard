package com.questboard.dto.achievement;

import com.questboard.domain.Achievement;

public record AchievementResponse(
        String code, String name, String description,
        long targetValue, String metric,
        int rewardXp, int rewardCoins,
        long progress, boolean completed
) {
    public static AchievementResponse from(Achievement a, long progress, boolean completed) {
        return new AchievementResponse(a.getCode(), a.getName(), a.getDescription(),
                a.getTargetValue(), a.getMetric(), a.getRewardXp(), a.getRewardCoins(),
                progress, completed);
    }
}