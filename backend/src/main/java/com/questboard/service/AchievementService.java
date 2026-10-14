package com.questboard.service;

import com.questboard.domain.Achievement;
import com.questboard.domain.User;
import com.questboard.domain.UserAchievement;
import com.questboard.repository.AchievementRepository;
import com.questboard.repository.QuestRepository;
import com.questboard.repository.UserAchievementRepository;
import com.questboard.repository.XpEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementService {

    public static final String METRIC_QUESTS_COMPLETED = "QUESTS_COMPLETED";
    public static final String METRIC_XP_EARNED        = "XP_EARNED";
    public static final String METRIC_STREAK_DAYS      = "STREAK_DAYS";
    public static final String METRIC_BOSSES_DEFEATED  = "BOSSES_DEFEATED";

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final QuestRepository questRepository;
    private final XpEventRepository xpEventRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Recalcula o progresso de todos os achievements do usuário.
     * Idempotente.
     */
    @Transactional
    public void evaluate(User user) {
        List<Achievement> all = achievementRepository.findAll();
        for (Achievement a : all) {
            long progress = computeProgress(user, a.getMetric());
            UserAchievement ua = userAchievementRepository
                    .findByUserIdAndAchievementId(user.getId(), a.getId())
                    .orElseGet(() -> UserAchievement.builder()
                            .user(user).achievement(a).progress(0).completed(false).build());

            boolean wasCompleted = ua.isCompleted();
            ua.setProgress(progress);

            if (!wasCompleted && progress >= a.getTargetValue()) {
                ua.setCompleted(true);
                ua.setCompletedAt(Instant.now());
                userAchievementRepository.save(ua);

                log.info("🏆 User {} completou achievement {}", user.getUsername(), a.getCode());
                eventPublisher.publishEvent(new AchievementCompletedEvent(
                        user.getId(), user.getUsername(), a.getCode(), a.getName(),
                        a.getRewardXp(), a.getRewardCoins()
                ));
                // NOTA: rewardXp/rewardCoins serão aplicados pelo listener que chama XpService
                // (evita ciclo XpService -> AchievementService -> XpService).
            } else {
                userAchievementRepository.save(ua);
            }
        }
    }

    private long computeProgress(User user, String metric) {
        return switch (metric) {
            case METRIC_QUESTS_COMPLETED ->
                    questRepository.countByAssigneeIdAndStatus(user.getId(),
                            com.questboard.domain.enums.QuestStatus.COMPLETED);
            case METRIC_XP_EARNED -> user.getTotalXp();
            case METRIC_STREAK_DAYS -> user.getLongestStreak();
            case METRIC_BOSSES_DEFEATED -> 0L; // TODO Bloco 3 (via BossFightService)
            default -> 0L;
        };
    }

    public record AchievementCompletedEvent(
            java.util.UUID userId, String username,
            String code, String name, int rewardXp, int rewardCoins) {}
}