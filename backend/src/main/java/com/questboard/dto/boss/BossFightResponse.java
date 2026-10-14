package com.questboard.dto.boss;

import com.questboard.domain.BossFight;

import java.time.Instant;
import java.util.UUID;

public record BossFightResponse(
        UUID id,
        String name,
        String description,
        String imageUrl,
        long totalHp,
        long currentHp,
        double hpPercent,
        String status,
        Instant startsAt,
        Instant endsAt,
        UUID guildId,
        Instant createdAt
) {
    public static BossFightResponse from(BossFight b) {
        double pct = b.getTotalHp() > 0
                ? (double) b.getCurrentHp() / b.getTotalHp() * 100.0
                : 0.0;
        return new BossFightResponse(
                b.getId(), b.getName(), b.getDescription(), b.getImageUrl(),
                b.getTotalHp(), b.getCurrentHp(), Math.round(pct * 100.0) / 100.0,
                b.getStatus().name(), b.getStartsAt(), b.getEndsAt(),
                b.getGuild() != null ? b.getGuild().getId() : null,
                b.getCreatedAt()
        );
    }
}