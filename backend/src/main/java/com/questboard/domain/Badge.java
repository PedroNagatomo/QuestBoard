package com.questboard.domain;

import com.questboard.domain.enums.Rarity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "badges")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Badge {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rarity rarity = Rarity.COMMON;

    @Column(name = "xp_bonus", nullable = false)
    private int xpBonus = 0;
}