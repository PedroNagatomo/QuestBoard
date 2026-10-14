package com.questboard.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_badges", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","badge_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserBadge {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "badge_id")
    private Badge badge;

    @Column(name = "awarded_at", nullable = false)
    @Builder.Default
    private Instant awardedAt = Instant.now();

    @PrePersist
    void prePersist() {
        if (awardedAt == null) awardedAt = Instant.now();
    }
}