package com.questboard.domain;

import com.questboard.domain.enums.Difficulty;
import com.questboard.domain.enums.QuestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "quests")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Quest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty = Difficulty.EASY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestStatus status = QuestStatus.OPEN;

    @Column(name = "xp_reward", nullable = false)
    private int xpReward;

    @Column(name = "coin_reward", nullable = false)
    private int coinReward;

    @Column(name = "due_date")
    private Instant dueDate;

    @Column(name = "completed_at")
    private Instant completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guild_id")
    private Guild guild;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_boss_id")
    private BossFight parentBoss;

    @Column(name = "external_source", length = 30)
    private String externalSource;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "external_url", length = 500)
    private String externalUrl;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private List<String> tags = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    private Long version;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }
}