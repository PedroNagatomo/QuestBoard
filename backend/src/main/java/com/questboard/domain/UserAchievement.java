package com.questboard.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_achievements",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","achievement_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserAchievement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "achievement_id")
    private Achievement achievement;

    @Column(nullable = false)
    private long progress = 0;

    @Column(nullable = false)
    private boolean completed = false;

    @Column(name = "completed_at")
    private Instant completedAt;
}