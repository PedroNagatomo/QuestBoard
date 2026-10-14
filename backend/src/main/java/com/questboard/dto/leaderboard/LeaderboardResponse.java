package com.questboard.dto.leaderboard;

import java.util.List;

public record LeaderboardResponse(
        String period,     // week, month, all-time
        String scope,      // global, guild, class
        List<LeaderboardEntry> entries,
        long generatedAt
) {}