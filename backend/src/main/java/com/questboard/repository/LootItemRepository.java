package com.questboard.repository;

import com.questboard.domain.LootItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LootItemRepository extends JpaRepository<LootItem, UUID> {

    Page<LootItem> findByActiveTrue(Pageable pageable);

    Page<LootItem> findByActiveTrueAndRarity(com.questboard.domain.enums.Rarity rarity, Pageable pageable);
}