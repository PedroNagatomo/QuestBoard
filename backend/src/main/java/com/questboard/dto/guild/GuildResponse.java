package com.questboard.dto.guild;

import com.questboard.domain.Guild;

import java.time.Instant;
import java.util.UUID;

public record GuildResponse(
        UUID id,
        String name,
        String description,
        String emblemUrl,
        long totalXp,
        int level,
        UUID ownerId,
        Instant createdAt
) {
    public static GuildResponse from(Guild g) {
        return new GuildResponse(g.getId(), g.getName(), g.getDescription(), g.getEmblemUrl(),
                g.getTotalXp(), g.getLevel(), g.getOwnerId(), g.getCreatedAt());
    }
}