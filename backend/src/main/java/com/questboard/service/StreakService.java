package com.questboard.service;

import com.questboard.domain.User;
import com.questboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreakService {

    private final UserRepository userRepository;
    private final XpService xpService;

    /**
     * Registra atividade do dia e atualiza streak.
     * Deve ser chamado quando o usuário completa alguma quest.
     *
     * @return streak atual após atualização
     */
    @Transactional
    public int registerActivity(User user) {
        LocalDate today = LocalDate.now();
        LocalDate last = user.getLastActiveDate();

        if (last == null) {
            user.setCurrentStreak(1);
        } else if (last.equals(today)) {
            // já registrou hoje — no-op
            return user.getCurrentStreak();
        } else if (last.equals(today.minusDays(1))) {
            user.setCurrentStreak(user.getCurrentStreak() + 1);
        } else {
            // quebrou streak
            user.setCurrentStreak(1);
        }

        if (user.getCurrentStreak() > user.getLongestStreak()) {
            user.setLongestStreak(user.getCurrentStreak());
        }
        user.setLastActiveDate(today);
        userRepository.save(user);

        // Bônus de milestone
        applyStreakBonus(user);

        log.debug("🔥 Streak de {} → {}", user.getUsername(), user.getCurrentStreak());
        return user.getCurrentStreak();
    }

    private void applyStreakBonus(User user) {
        int streak = user.getCurrentStreak();
        int bonusXp = 0;
        int bonusCoins = 0;

        if (streak > 0 && streak % 30 == 0) {
            bonusXp = 500; bonusCoins = 200;
        } else if (streak > 0 && streak % 7 == 0) {
            bonusXp = 100; bonusCoins = 40;
        } else if (streak > 0 && streak % 3 == 0) {
            bonusXp = 25; bonusCoins = 10;
        }

        if (bonusXp > 0) {
            // Chama XpService sem anti-cheat de QUEST (usa MANUAL) — o próprio serviço
            // já valida cap diário. O tipo STREAK_BONUS não é QUEST, então passa.
            xpService.grantXp(user.getId(), bonusXp, bonusCoins,
                    com.questboard.domain.enums.XpSourceType.STREAK_BONUS,
                    null, "Streak de " + streak + " dias");
            log.info("🎁 Bônus de streak {}d para {}: +{} XP / +{} coins",
                    streak, user.getUsername(), bonusXp, bonusCoins);
        }
    }
}