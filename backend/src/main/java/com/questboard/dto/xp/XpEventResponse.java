package com.questboard.dto.xp;

import com.questboard.domain.XpEvent;

import java.time.Instant;
import java.util.UUID;

public record XpEventResponse(
        UUID id,
        String sourceType,
        UUID sourceId,
        int xpDelta,
        int coinDelta,
        String reason,
        Instant createdAt
) {
    public static XpEventResponse from(XpEvent e) {
        return new XpEventResponse(e.getId(), e.getSourceType().name(), e.getSourceId(),
                e.getXpDelta(), e.getCoinDelta(), e.getReason(), e.getCreatedAt());
    }
}