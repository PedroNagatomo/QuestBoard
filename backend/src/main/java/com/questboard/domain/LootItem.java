package com.questboard.domain;

import com.questboard.domain.enums.Rarity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "loot_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LootItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "cost_coins", nullable = false)
    private int costCoins;

    private Integer stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rarity rarity = Rarity.COMMON;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}