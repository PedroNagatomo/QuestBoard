package com.questboard.dto.badge;

import com.questboard.domain.Badge;

import java.time.Instant;

public record BadgeResponse(
        String code, String name, String description, String iconUrl,
        String rarity, int xpBonus, Instant awardedAt
) {
    public static BadgeResponse from(Badge b, Instant awardedAt) {
        return new BadgeResponse(b.getCode(), b.getName(), b.getDescription(), b.getIconUrl(),
                b.getRarity().name(), b.getXpBonus(), awardedAt);
    }
}