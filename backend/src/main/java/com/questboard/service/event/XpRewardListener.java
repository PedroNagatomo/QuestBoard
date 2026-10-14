package com.questboard.service.event;

import com.questboard.domain.enums.XpSourceType;
import com.questboard.dto.leaderboard.LeaderboardResponse;
import com.questboard.service.AchievementService;
import com.questboard.service.BadgeService;
import com.questboard.service.LeaderboardService;
import com.questboard.service.XpService;
import com.questboard.websocket.RedisWebSocketBridge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class XpRewardListener {

    private final XpService xpService;
    private final LeaderboardService leaderboardService;
    private final RedisWebSocketBridge bridge;

    /** XP concedido → atualiza leaderboard Redis + notifica usuário + broadcast TOP 10. */
    @Async
    @EventListener
    public void onXpGranted(XpService.XpGrantedEvent ev) {
        if (ev.xpDelta() <= 0) return;

        try {
            leaderboardService.addScore(ev.userId(), ev.xpDelta());
        } catch (Exception e) {
            log.warn("Leaderboard Redis indisponível: {}", e.getMessage());
        }

        // Notificação pessoal
        Map<String, Object> data = new HashMap<>();
        data.put("xpDelta", ev.xpDelta());
        data.put("coinDelta", ev.coinDelta());
        data.put("source", ev.sourceType().name());
        data.put("level", ev.newLevel());
        data.put("levelsGained", ev.levelsGained());
        data.put("xp", ev.currentXp());
        data.put("xpToNext", ev.xpToNext());
        data.put("leveledUp", ev.levelsGained() > 0);

        String type = ev.levelsGained() > 0 ? "LEVEL_UP" : "XP_GAINED";
        bridge.notifyUser(ev.userId(), type, data);

        // Broadcast do TOP 10
        try {
            LeaderboardResponse top = leaderboardService.top("all-time", "global", null, 10);
            Map<String, Object> payload = new HashMap<>();
            payload.put("period", "all-time");
            payload.put("entries", top.entries());
            payload.put("generatedAt", top.generatedAt());
            bridge.broadcastLeaderboard("all-time", payload);
        } catch (Exception e) {
            log.debug("Falha ao publicar leaderboard: {}", e.getMessage());
        }
    }

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAchievementCompleted(AchievementService.AchievementCompletedEvent ev) {
        try {
            if (ev.rewardXp() > 0 || ev.rewardCoins() > 0) {
                xpService.grantXp(ev.userId(), ev.rewardXp(), ev.rewardCoins(),
                        XpSourceType.ACHIEVEMENT, null,
                        "Achievement: " + ev.code());
            }
            bridge.notifyUser(ev.userId(), "ACHIEVEMENT_UNLOCKED", Map.of(
                    "code", ev.code(),
                    "name", ev.name(),
                    "rewardXp", ev.rewardXp(),
                    "rewardCoins", ev.rewardCoins()
            ));
        } catch (Exception e) {
            log.error("Erro ao processar achievement {}", ev.code(), e);
        }
    }

    @Async
    @EventListener
    public void onBadgeAwarded(BadgeService.BadgeAwardedEvent ev) {
        bridge.notifyUser(ev.userId(), "BADGE_UNLOCKED", Map.of(
                "code", ev.badgeCode(),
                "name", ev.badgeName(),
                "rarity", ev.rarity(),
                "iconUrl", ev.iconUrl() != null ? ev.iconUrl() : ""
        ));
    }

    @Async
    @EventListener
    public void onBossDefeated(com.questboard.service.BossFightService.BossDefeatedEvent ev) {
        // Broadcast público — todos veem que um boss caiu
        bridge.broadcastLeaderboard("boss-defeated", Map.of(
                "bossId", ev.bossId().toString(),
                "bossName", ev.bossName()
        ));
    }
}