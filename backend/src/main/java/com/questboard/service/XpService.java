package com.questboard.service;

import com.questboard.domain.User;
import com.questboard.domain.XpEvent;
import com.questboard.domain.enums.XpSourceType;
import com.questboard.exception.BadRequestException;
import com.questboard.exception.NotFoundException;
import com.questboard.repository.UserRepository;
import com.questboard.repository.XpEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class XpService {

    private final UserRepository userRepository;
    private final XpEventRepository xpEventRepository;
    private final LevelService levelService;
    private final AntiCheatService antiCheatService;
    private final BadgeService badgeService;
    private final AchievementService achievementService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Concede XP e coins a um usuário. Idempotência deve ser tratada pelo chamador
     * (ex: não completar a mesma quest duas vezes).
     *
     * @param userId      usuário alvo
     * @param xpDelta     XP a conceder (pode ser negativo para penalidade — não recomendado)
     * @param coinDelta   coins a conceder
     * @param sourceType  origem
     * @param sourceId    id da origem (questId, badgeId, etc.)
     * @param reason      texto livre para auditoria
     */
    @Transactional
    public XpGrantResult grantXp(UUID userId, int xpDelta, int coinDelta,
                                 XpSourceType sourceType, UUID sourceId, String reason) {

        if (xpDelta == 0 && coinDelta == 0) {
            throw new BadRequestException("xpDelta e coinDelta não podem ser ambos zero");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));

        // Anti-cheat: se xp positivo, valida rate/cap
        if (xpDelta > 0) {
            antiCheatService.validateXpGrant(user, xpDelta, sourceType);
        }

        // Aplica XP + level
        int oldLevel = user.getLevel();
        LevelService.LevelUpResult result = levelService.applyXp(oldLevel, user.getXp(), xpDelta);

        user.setXp(result.remainingXp());
        user.setLevel(result.newLevel());
        user.setTotalXp(user.getTotalXp() + Math.max(xpDelta, 0));
        user.setCoins(user.getCoins() + coinDelta);

        // Streak (só em XP positivo)
        if (xpDelta > 0) {
            // StreakService é chamado por QuestService/outros serviços que já sabem o contexto
            // Aqui só reforçamos a data de atividade
            user.setLastActiveDate(java.time.LocalDate.now());
        }

        userRepository.save(user);

        // Auditoria
        XpEvent event = XpEvent.builder()
                .user(user)
                .sourceType(sourceType)
                .sourceId(sourceId)
                .xpDelta(xpDelta)
                .coinDelta(coinDelta)
                .reason(reason)
                .build();
        xpEventRepository.save(event);

        // Badges e achievements reagem a XP/nível/quest
        badgeService.evaluate(user, sourceType);
        achievementService.evaluate(user);

        // Publica evento para WebSocket/Redis
        eventPublisher.publishEvent(new XpGrantedEvent(
                user.getId(), user.getUsername(), xpDelta, coinDelta,
                sourceType, result.newLevel(), result.levelsGained(),
                result.remainingXp(), levelService.xpToNextLevel(result.newLevel())
        ));

        if (result.leveledUp()) {
            log.info("⬆️  User {} subiu {} nível(is) → {}", user.getUsername(), result.levelsGained(), result.newLevel());
        }

        return new XpGrantResult(
                result.newLevel(), result.levelsGained(),
                user.getXp(), levelService.xpToNextLevel(user.getLevel()),
                xpDelta, coinDelta
        );
    }

    public record XpGrantResult(int newLevel, int levelsGained, long xp, long xpToNext,
                                int xpDelta, int coinDelta) {}

    /** Evento interno para outros componentes (WebSocket, notificações). */
    public record XpGrantedEvent(
            UUID userId, String username,
            int xpDelta, int coinDelta,
            XpSourceType sourceType,
            int newLevel, int levelsGained,
            long currentXp, long xpToNext
    ) {}
}