package com.questboard.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "integrations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider","guild_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Integration {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 30)
    private String provider;

    @Column(name = "guild_id")
    private UUID guildId;

    @Column(name = "config_json", columnDefinition = "jsonb", nullable = false)
    private String configJson = "{}";

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