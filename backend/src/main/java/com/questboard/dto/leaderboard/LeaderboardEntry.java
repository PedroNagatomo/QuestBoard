package com.questboard.dto.leaderboard;

import java.util.UUID;

public record LeaderboardEntry(
        int rank,
        UUID userId,
        String username,
        String displayName,
        String avatarUrl,
        String playerClass,
        int level,
        long score
) {}