package com.questboard.repository;

import com.questboard.domain.BossFight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BossFightRepository extends JpaRepository<BossFight, UUID> {

    List<BossFight> findByStatus(BossFight.BossStatus status);

    List<BossFight> findByGuildId(UUID guildId);

    List<BossFight> findByStatusAndGuildId(BossFight.BossStatus status, UUID guildId);
}