package com.questboard.controller;

import com.questboard.dto.leaderboard.LeaderboardResponse;
import com.questboard.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping
    public LeaderboardResponse top(
            @RequestParam(defaultValue = "all-time") String period,
            @RequestParam(defaultValue = "global")   String scope,
            @RequestParam(required = false) UUID scopeId,
            @RequestParam(defaultValue = "20") int limit) {
        return leaderboardService.top(period, scope, scopeId, Math.min(limit, 100));
    }

    @PostMapping("/rebuild")
    public void rebuild() {
        leaderboardService.rebuildAllTime();
    }
}