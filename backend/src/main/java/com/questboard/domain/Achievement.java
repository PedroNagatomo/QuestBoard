package com.questboard.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "achievements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Achievement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "target_value", nullable = false)
    private long targetValue = 1;

    @Column(nullable = false, length = 50)
    private String metric; // QUESTS_COMPLETED, XP_EARNED, STREAK_DAYS, BOSSES_DEFEATED

    @Column(name = "reward_xp", nullable = false)
    private int rewardXp = 0;

    @Column(name = "reward_coins", nullable = false)
    private int rewardCoins = 0;
}