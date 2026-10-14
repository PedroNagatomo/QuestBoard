package com.questboard.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "loot_redemptions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LootRedemption {
    public enum Status { PENDING, APPROVED, DELIVERED, REJECTED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loot_id")
    private LootItem loot;

    @Column(name = "cost_paid", nullable = false)
    private int costPaid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(name = "redeemed_at", nullable = false)
    @Builder.Default
    private Instant redeemedAt = Instant.now();

    @PrePersist
    void prePersist() {
        if (redeemedAt == null) redeemedAt = Instant.now();
    }
}