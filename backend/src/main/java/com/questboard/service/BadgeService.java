package com.questboard.service;

import com.questboard.domain.Badge;
import com.questboard.domain.User;
import com.questboard.domain.UserBadge;
import com.questboard.domain.enums.XpSourceType;
import com.questboard.repository.BadgeRepository;
import com.questboard.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final com.questboard.repository.QuestRepository questRepository;

    @Transactional
    public void evaluate(User user, XpSourceType sourceType) {
        switch (sourceType) {
            case QUEST -> {
                long questsCompleted = countQuestsCompleted(user);
                if (questsCompleted >= 1) award(user, "FIRST_BLOOD");
                if (questsCompleted >= 10) award(user, "QUEST_10");
                if (questsCompleted >= 100) award(user, "QUEST_100");
            }
            case BOSS -> award(user, "BOSS_SLAYER");
            case STREAK_BONUS -> {
                if (user.getCurrentStreak() >= 7) award(user, "STREAK_7");
                if (user.getCurrentStreak() >= 30) award(user, "STREAK_30");
            }
            default -> { /* no-op */ }
        }
    }

    private long countQuestsCompleted(User user) {
        return questRepository.countByAssigneeIdAndStatus(
                user.getId(),
                com.questboard.domain.enums.QuestStatus.COMPLETED);
    }

    @Transactional
    public void award(User user, String badgeCode) {
        Badge badge = badgeRepository.findByCode(badgeCode).orElse(null);
        if (badge == null) return;

        if (userBadgeRepository.existsByUserIdAndBadgeId(user.getId(), badge.getId())) {
            return; // já possui
        }

        UserBadge ub = UserBadge.builder().user(user).badge(badge).build();
        userBadgeRepository.save(ub);

        // Bônus de XP do badge (se houver)
        if (badge.getXpBonus() > 0) {
        }

        eventPublisher.publishEvent(new BadgeAwardedEvent(user.getId(), user.getUsername(),
                badge.getCode(), badge.getName(), badge.getIconUrl(), badge.getRarity().name()));

        log.info("🏅 User {} ganhou badge {}", user.getUsername(), badgeCode);
    }

    public List<UserBadge> listUserBadges(User user) {
        return userBadgeRepository.findByUserIdWithBadge(user.getId());
    }
    public record BadgeAwardedEvent(java.util.UUID userId, String username,
                                    String badgeCode, String badgeName,
                                    String iconUrl, String rarity) {}
}