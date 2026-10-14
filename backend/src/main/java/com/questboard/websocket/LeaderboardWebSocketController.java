package com.questboard.websocket;

import com.questboard.dto.leaderboard.LeaderboardResponse;
import com.questboard.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class LeaderboardWebSocketController {

    private final LeaderboardService leaderboardService;

    /**
     * Cliente manda para /app/leaderboard/snapshot
     * e recebe em /topic/leaderboard/snapshot
     * Body: {"period":"all-time","limit":20}
     */
    @MessageMapping("/leaderboard/snapshot")
    @SendTo("/topic/leaderboard/snapshot")
    public LeaderboardResponse snapshot(Map<String, Object> req) {
        String period = (String) req.getOrDefault("period", "all-time");
        int limit = req.get("limit") instanceof Number n ? n.intValue() : 20;
        return leaderboardService.top(period, "global", null, Math.min(limit, 100));
    }
}