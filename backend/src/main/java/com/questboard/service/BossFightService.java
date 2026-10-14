package com.questboard.service;

import com.questboard.domain.BossFight;
import com.questboard.domain.User;
import com.questboard.domain.enums.XpSourceType;
import com.questboard.dto.boss.BossFightResponse;
import com.questboard.dto.boss.CreateBossRequest;
import com.questboard.dto.boss.DamageBossRequest;
import com.questboard.exception.BadRequestException;
import com.questboard.exception.NotFoundException;
import com.questboard.repository.BossFightRepository;
import com.questboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BossFightService {

    private final BossFightRepository bossRepository;
    private final UserRepository userRepository;
    private final GuildService guildService;
    private final XpService xpService;
    private final ApplicationEventPublisher eventPublisher;

    public BossFight require(UUID id) {
        return bossRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("BossFight", id));
    }

    @Transactional
    public BossFightResponse create(UUID creatorId, CreateBossRequest req) {
        BossFight.BossFightBuilder b = BossFight.builder()
                .name(req.name())
                .description(req.description())
                .imageUrl(req.imageUrl())
                .totalHp(req.totalHp())
                .currentHp(req.totalHp())
                .status(BossFight.BossStatus.ACTIVE)
                .startsAt(req.startsAt() != null ? req.startsAt() : Instant.now())
                .endsAt(req.endsAt())
                .createdById(creatorId);

        if (req.guildId() != null) {
            b.guild(guildService.require(req.guildId()));
        }

        BossFight saved = bossRepository.save(b.build());
        log.info("🐉 Boss criado: {} (HP {})", saved.getName(), saved.getTotalHp());
        return BossFightResponse.from(saved);
    }

    public List<BossFightResponse> listActive() {
        return bossRepository.findByStatus(BossFight.BossStatus.ACTIVE).stream()
                .map(BossFightResponse::from).toList();
    }

    public List<BossFightResponse> listByGuild(UUID guildId) {
        return bossRepository.findByGuildId(guildId).stream()
                .map(BossFightResponse::from).toList();
    }

    /**
     * Aplica dano ao boss. Quando HP chega a 0, distribui XP para a guilda e marca como DEFEATED.
     */
    @Transactional
    public BossFightResponse damage(UUID bossId, long damage) {
        BossFight b = require(bossId);
        if (b.getStatus() != BossFight.BossStatus.ACTIVE) {
            throw new BadRequestException("Boss não está ativo");
        }
        if (damage <= 0) return BossFightResponse.from(b);

        long newHp = Math.max(0, b.getCurrentHp() - damage);
        b.setCurrentHp(newHp);

        if (newHp == 0 && b.getStatus() == BossFight.BossStatus.ACTIVE) {
            b.setStatus(BossFight.BossStatus.DEFEATED);
            log.info("💀 Boss derrotado: {}", b.getName());

            // Recompensa coletiva — divide um bônus entre os membros da guilda
            if (b.getGuild() != null) {
                long bonusXp = b.getTotalHp() / 10;
                long bonusCoins = b.getTotalHp() / 20;
                var members = userRepository.findByGuildOrderByTotalXp(b.getGuild().getId());
                if (!members.isEmpty()) {
                    long xpEach = bonusXp / members.size();
                    long coinEach = bonusCoins / members.size();
                    for (User u : members) {
                        xpService.grantXp(u.getId(), (int) xpEach, (int) coinEach,
                                XpSourceType.BOSS, b.getId(),
                                "Boss derrotado: " + b.getName());
                    }
                }
            }
            eventPublisher.publishEvent(new BossDefeatedEvent(b.getId(), b.getName()));
        }

        bossRepository.save(b);
        return BossFightResponse.from(b);
    }

    /** Atalho para dano pontual via REST. */
    @Transactional
    public BossFightResponse applyDamage(UUID bossId, DamageBossRequest req) {
        return damage(bossId, req.damage());
    }

    public record BossDefeatedEvent(UUID bossId, String bossName) {}
}