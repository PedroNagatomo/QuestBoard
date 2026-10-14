package com.questboard.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "boss_fights")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder   // ← @Builder aqui
public class BossFight {

    public enum BossStatus { ACTIVE, DEFEATED, EXPIRED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "total_hp", nullable = false)
    private long totalHp;

    @Column(name = "current_hp", nullable = false)
    private long currentHp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BossStatus status = BossStatus.ACTIVE;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guild_id")
    private Guild guild;

    @Column(name = "created_by_id", nullable = false)
    private UUID createdById;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }
}