package com.questboard.service;

import com.questboard.domain.Quest;
import com.questboard.domain.User;
import com.questboard.domain.enums.Difficulty;
import com.questboard.domain.enums.QuestStatus;
import com.questboard.domain.enums.XpSourceType;
import com.questboard.dto.quest.*;
import com.questboard.exception.BadRequestException;
import com.questboard.exception.ForbiddenException;
import com.questboard.exception.NotFoundException;
import com.questboard.repository.QuestRepository;
import com.questboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestService {

    private final QuestRepository questRepository;
    private final UserRepository userRepository;
    private final GuildService guildService;
    private final XpService xpService;
    private final StreakService streakService;
    private final AntiCheatService antiCheatService;
    private final BossFightService bossFightService;

    // ---------- CRUD ----------

    @Transactional
    public QuestResponse create(UUID creatorId, CreateQuestRequest req) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new NotFoundException("User", creatorId));

        Difficulty difficulty = req.difficulty() != null ? req.difficulty() : Difficulty.EASY;
        int xp = req.xpOverride() != null ? req.xpOverride() : difficulty.getBaseXp();
        int coins = req.coinOverride() != null ? req.coinOverride() : difficulty.getBaseCoins();

        Quest.QuestBuilder b = Quest.builder()
                .title(req.title())
                .description(req.description())
                .difficulty(difficulty)
                .status(QuestStatus.OPEN)
                .xpReward(xp)
                .coinReward(coins)
                .dueDate(req.dueDate())
                .createdBy(creator)
                .externalSource("MANUAL");

        if (req.assigneeId() != null) {
            User assignee = userRepository.findById(req.assigneeId())
                    .orElseThrow(() -> new NotFoundException("User", req.assigneeId()));
            b.assignee(assignee);
        }
        if (req.guildId() != null) {
            b.guild(guildService.require(req.guildId()));
        }
        if (req.tags() != null) {
            b.tags(req.tags());
        }

        Quest saved = questRepository.save(b.build());
        log.info("📜 Quest criada: {} (id={}) por {}", saved.getTitle(), saved.getId(), creator.getUsername());
        return QuestResponse.from(saved);
    }

    public Quest require(UUID id) {
        return questRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new NotFoundException("Quest", id));
    }

    public QuestResponse get(UUID id) {
        return QuestResponse.from(require(id));
    }

    public Page<QuestResponse> list(QuestStatus status, UUID assigneeId,
                                    UUID guildId, Pageable pageable) {
        if (assigneeId != null) {
            return questRepository.findByAssigneeIdWithRelations(assigneeId, pageable).map(QuestResponse::from);
        }
        if (guildId != null) {
            return questRepository.findByGuildIdWithRelations(guildId, pageable).map(QuestResponse::from);
        }
        if (status != null) {
            return questRepository.findByStatusWithRelations(status, pageable).map(QuestResponse::from);
        }
        return questRepository.findAllWithRelations(pageable).map(QuestResponse::from);
    }

    @Transactional
    public QuestResponse update(UUID questId, UUID actorId, UpdateQuestRequest req) {
        Quest q = require(questId);
        ensureCanEdit(q, actorId);

        if (req.title() != null)       q.setTitle(req.title());
        if (req.description() != null) q.setDescription(req.description());
        if (req.difficulty() != null)  q.setDifficulty(req.difficulty());
        if (req.status() != null)      q.setStatus(req.status());
        if (req.dueDate() != null)     q.setDueDate(req.dueDate());
        if (req.tags() != null)        q.setTags(req.tags());
        if (req.assigneeId() != null) {
            User assignee = userRepository.findById(req.assigneeId())
                    .orElseThrow(() -> new NotFoundException("User", req.assigneeId()));
            q.setAssignee(assignee);
        }
        questRepository.save(q);
        return QuestResponse.from(q);
    }

    @Transactional
    public QuestResponse assign(UUID questId, UUID actorId, UUID assigneeId) {
        Quest q = require(questId);
        ensureCanEdit(q, actorId);
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new NotFoundException("User", assigneeId));
        q.setAssignee(assignee);
        if (q.getStatus() == QuestStatus.OPEN) q.setStatus(QuestStatus.IN_PROGRESS);
        questRepository.save(q);
        return QuestResponse.from(q);
    }

    @Transactional
    public QuestResponse start(UUID questId, UUID actorId) {
        Quest q = require(questId);
        if (q.getStatus() != QuestStatus.OPEN) {
            throw new BadRequestException("Quest não está OPEN");
        }
        ensureCanEdit(q, actorId);
        q.setStatus(QuestStatus.IN_PROGRESS);
        questRepository.save(q);
        return QuestResponse.from(q);
    }

    /**
     * COMPLETA a quest e dispara recompensas.
     *
     * Fluxo:
     *   1. valida status e permissão
     *   2. anti-cheat (tempo mínimo)
     *   3. marca como COMPLETED
     *   4. concede XP/coins via XpService (que já checa anti-cheat, level, badges, achievements)
     *   5. atualiza streak (via StreakService)
     *   6. se vinculada a boss, aplica dano (via BossFightService — cuidado com ciclo, ver nota)
     *   7. se vinculada a guild, soma totalXp
     *   8. publica evento de domínio
     */
    @Transactional
    public QuestResponse complete(UUID questId, UUID actorId, CompleteQuestRequest req) {
        Quest q = require(questId);

        if (q.getStatus() == QuestStatus.COMPLETED) {
            throw new BadRequestException("Quest já concluída");
        }
        if (q.getStatus() == QuestStatus.CANCELLED || q.getStatus() == QuestStatus.FAILED) {
            throw new BadRequestException("Quest não pode ser concluída no status " + q.getStatus());
        }
        // Quem completa: o próprio assignee, o criador, ou (no futuro) admin
        if (q.getAssignee() != null && !q.getAssignee().getId().equals(actorId)
                && !q.getCreatedBy().getId().equals(actorId)) {
            throw new ForbiddenException("Você não pode completar esta quest");
        }

        // Anti-cheat: tempo mínimo entre criação e conclusão
        antiCheatService.validateQuestCompletionTime(q.getCreatedAt(), q.getDueDate());

        User assignee = q.getAssignee() != null ? q.getAssignee() : q.getCreatedBy();

        q.setStatus(QuestStatus.COMPLETED);
        q.setCompletedAt(Instant.now());
        questRepository.save(q);

        // Concede XP — ATENÇÃO: este é o ponto onde XP + level + badges + achievements
        // são resolvidos. BadgeService chama QuestRepository para contar quests.
        xpService.grantXp(
                assignee.getId(),
                q.getXpReward(),
                q.getCoinReward(),
                XpSourceType.QUEST,
                q.getId(),
                "Quest: " + q.getTitle()
        );

        // Streak — se o usuário tem atividade hoje, atualiza
        streakService.registerActivity(assignee);

        // Guild — soma XP agregado
        if (q.getGuild() != null) {
            guildService.addXp(q.getGuild().getId(), q.getXpReward());
        }

        // Boss — dano proporcional ao XP da quest
        if (q.getParentBoss() != null) {
            // Ver nota abaixo sobre ciclo
            applyBossDamage(q.getParentBoss().getId(), q.getXpReward());
        }

        log.info("⚔️  Quest concluída: {} por {} (+{} XP, +{} coins)",
                q.getTitle(), assignee.getUsername(), q.getXpReward(), q.getCoinReward());

        return QuestResponse.from(q);
    }

    @Transactional
    public QuestResponse cancel(UUID questId, UUID actorId) {
        Quest q = require(questId);
        ensureCanEdit(q, actorId);
        q.setStatus(QuestStatus.CANCELLED);
        questRepository.save(q);
        return QuestResponse.from(q);
    }

    @Transactional
    public void delete(UUID questId, UUID actorId) {
        Quest q = require(questId);
        ensureCanEdit(q, actorId);
        questRepository.delete(q);
    }

    // ---------- helpers ----------

    private void ensureCanEdit(Quest q, UUID actorId) {
        boolean isCreator  = q.getCreatedBy().getId().equals(actorId);
        boolean isAssignee = q.getAssignee() != null && q.getAssignee().getId().equals(actorId);
        if (!isCreator && !isAssignee) {
            throw new ForbiddenException("Você não pode alterar esta quest");
        }
    }

    /**
     * NOTA sobre ciclo QuestService <-> BossFightService:
     * BossFightService precisa do QuestService? Não. Ele só mexe em HP.
     * Então injetamos BossFightService aqui SEM ciclo. Porém, para não ter
     * acoplamento direto, poderíamos usar ApplicationEventPublisher. Deixei
     * direto por simplicidade — se o BossFightService um dia precisar de
     * QuestService, trocamos por evento.
     */
    private void applyBossDamage(UUID bossId, long damage) {
        try {
            bossFightService.damage(bossId, damage);
        } catch (Exception e) {
            log.warn("Falha ao aplicar dano no boss {}: {}", bossId, e.getMessage());
        }
    }


}