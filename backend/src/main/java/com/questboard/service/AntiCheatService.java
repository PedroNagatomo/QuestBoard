package com.questboard.service;

import com.questboard.domain.User;
import com.questboard.domain.enums.XpSourceType;
import com.questboard.exception.AntiCheatException;
import com.questboard.repository.XpEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AntiCheatService {

    private final XpEventRepository xpEventRepository;

    @Value("${app.anti-cheat.max-quests-per-hour:10}")
    private int maxQuestsPerHour;

    @Value("${app.anti-cheat.min-seconds-to-complete:30}")
    private int minSecondsToComplete;

    @Value("${app.anti-cheat.xp-daily-cap:5000}")
    private long xpDailyCap;

    /**
     * Valida se um grant de XP faz sentido. Lança AntiCheatException se violar regras.
     */
    public void validateXpGrant(User user, int xpDelta, XpSourceType sourceType) {
        Instant since24h = Instant.now().minus(24, ChronoUnit.HOURS);

        // 1) Cap diário de XP positivo
        long xpToday = xpEventRepository.sumPositiveXpSince(user.getId(), since24h);
        if (xpToday + xpDelta > xpDailyCap) {
            log.warn("🚨 [ANTI-CHEAT] User {} excedeu cap diário: {} + {} > {}",
                    user.getUsername(), xpToday, xpDelta, xpDailyCap);
            throw new AntiCheatException(
                    "Limite diário de XP atingido (" + xpDailyCap + "). Continue amanhã!");
        }

        // 2) Rate limit de eventos do tipo QUEST (conclusão em massa)
        if (sourceType == XpSourceType.QUEST) {
            Instant since1h = Instant.now().minus(1, ChronoUnit.HOURS);
            long questsLastHour = xpEventRepository.countByUserIdAndCreatedAtAfter(user.getId(), since1h);
            if (questsLastHour >= maxQuestsPerHour) {
                log.warn("🚨 [ANTI-CHEAT] User {} concluiu {} quests na última hora (limite {})",
                        user.getUsername(), questsLastHour, maxQuestsPerHour);
                throw new AntiCheatException(
                        "Muitas quests concluídas em pouco tempo. Aguarde alguns minutos.");
            }
        }

        // 3) XP absurdo (possível bug/manipulação)
        if (xpDelta > 10_000) {
            log.warn("🚨 [ANTI-CHEAT] XP absurdo detectado para {}: {}", user.getUsername(), xpDelta);
            throw new AntiCheatException("Valor de XP inválido");
        }

        // 4) Usuário já com muitas flags
        long flags = xpEventRepository.countByUserIdAndFlaggedTrue(user.getId());
        if (flags >= 5) {
            log.warn("🚨 [ANTI-CHEAT] User {} possui {} flags. Bloqueado.", user.getUsername(), flags);
            throw new AntiCheatException("Conta sob revisão. Contate o administrador.");
        }
    }

    /** Valida tempo mínimo entre criação e conclusão de uma quest (evita spam). */
    public void validateQuestCompletionTime(Instant questCreatedAt, Instant questDueDate) {
        long seconds = ChronoUnit.SECONDS.between(questCreatedAt, Instant.now());
        if (seconds < minSecondsToComplete) {
            log.warn("🚨 [ANTI-CHEAT] Quest concluída em {}s (mínimo {}s)", seconds, minSecondsToComplete);
            throw new AntiCheatException("Quest concluída rápido demais — aguarde alguns segundos.");
        }
    }
}