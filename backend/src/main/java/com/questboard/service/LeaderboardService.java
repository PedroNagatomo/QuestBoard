package com.questboard.service;

import com.questboard.domain.User;
import com.questboard.dto.leaderboard.LeaderboardEntry;
import com.questboard.dto.leaderboard.LeaderboardResponse;
import com.questboard.repository.UserRepository;
import com.questboard.repository.XpEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaderboardService {

    public static final String KEY_ALL_TIME = "lb:all-time";
    public static final String KEY_WEEK     = "lb:week";
    public static final String KEY_MONTH    = "lb:month";

    private final RedisTemplate<String, Object> redis;
    private final UserRepository userRepository;
    private final XpEventRepository xpEventRepository;

    /** Empurra XP para os 3 leaderboards. Chamado no XpService (via listener). */
    public void addScore(UUID userId, long xp) {
        if (xp <= 0) return;
        try {
            redis.opsForZSet().incrementScore(KEY_ALL_TIME, userId.toString(), xp);
            redis.opsForZSet().incrementScore(KEY_WEEK, userId.toString(), xp);
            redis.opsForZSet().incrementScore(KEY_MONTH, userId.toString(), xp);
        } catch (Exception e) {
            log.warn("Redis indisponível para leaderboard: {}", e.getMessage());
        }
    }

    /** Reconstrói o ZSET all-time a partir do banco. Idempotente. */
    public void rebuildAllTime() {
        try {
            redis.delete(KEY_ALL_TIME);
            for (User u : userRepository.findTop100ByOrderByTotalXpDesc()) {
                redis.opsForZSet().add(KEY_ALL_TIME, u.getId().toString(), u.getTotalXp());
            }
            log.info("🔁 Leaderboard all-time reconstruído ({} usuários)", 100);
        } catch (Exception e) {
            log.warn("Falha ao reconstruir leaderboard: {}", e.getMessage());
        }
    }

    public LeaderboardResponse top(String period, String scope, UUID scopeId, int limit) {
        String key = switch (period == null ? "all-time" : period) {
            case "week"  -> KEY_WEEK;
            case "month" -> KEY_MONTH;
            default      -> KEY_ALL_TIME;
        };
        // Fallback para banco caso Redis esteja vazio ou indisponível
        Set<ZSetOperations.TypedTuple<Object>> tuples;
        try {
            tuples = redis.opsForZSet().reverseRangeWithScores(key, 0, limit - 1);
        } catch (Exception e) {
            log.warn("Redis indisponível, caindo no Postgres: {}", e.getMessage());
            return fromDatabase(period, limit);
        }

        if (tuples == null || tuples.isEmpty()) {
            return fromDatabase(period, limit);
        }

        List<LeaderboardEntry> entries = new ArrayList<>();
        int rank = 1;
        for (ZSetOperations.TypedTuple<Object> t : tuples) {
            if (t.getValue() == null) continue;
            UUID userId = UUID.fromString(t.getValue().toString());
            User u = userRepository.findById(userId).orElse(null);
            if (u == null) continue;
            entries.add(new LeaderboardEntry(
                    rank++, u.getId(), u.getUsername(), u.getDisplayName(),
                    u.getAvatarUrl(), u.getPlayerClass().name(),
                    u.getLevel(), t.getScore() == null ? 0 : t.getScore().longValue()
            ));
        }

        return new LeaderboardResponse(period, scope == null ? "global" : scope,
                entries, System.currentTimeMillis());
    }

    private LeaderboardResponse fromDatabase(String period, int limit) {
        var page = org.springframework.data.domain.PageRequest.of(0, limit);
        List<LeaderboardEntry> entries = new ArrayList<>();
        int rank = 1;
        Instant since = switch (period == null ? "all-time" : period) {
            case "week"  -> Instant.now().minus(7, ChronoUnit.DAYS);
            case "month" -> Instant.now().minus(30, ChronoUnit.DAYS);
            default      -> null;
        };

        // Se period=week/month usamos totalXp por enquanto (não temos agregação por janela
        // no Postgres de forma barata). Para agregação por janela, ver XpEventRepository.
        for (User u : userRepository.findAllByOrderByTotalXpDesc(page)) {
            entries.add(new LeaderboardEntry(
                    rank++, u.getId(), u.getUsername(), u.getDisplayName(),
                    u.getAvatarUrl(), u.getPlayerClass().name(), u.getLevel(), u.getTotalXp()
            ));
        }
        return new LeaderboardResponse(period, "global", entries, System.currentTimeMillis());
    }
}