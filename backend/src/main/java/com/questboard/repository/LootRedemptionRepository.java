package com.questboard.repository;

import com.questboard.domain.LootRedemption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LootRedemptionRepository extends JpaRepository<LootRedemption, UUID> {

    Page<LootRedemption> findByUserId(UUID userId, Pageable pageable);

    Page<LootRedemption> findByStatus(LootRedemption.Status status, Pageable pageable);
}