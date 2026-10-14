package com.questboard.domain;

import com.questboard.domain.enums.XpSourceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "xp_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class XpEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private XpSourceType sourceType;

    @Column(name = "source_id")
    private UUID sourceId;

    @Column(name = "xp_delta", nullable = false)
    private int xpDelta;

    @Column(name = "coin_delta", nullable = false)
    private int coinDelta = 0;

    private String reason;

    @Column(nullable = false)
    private boolean flagged = false;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}