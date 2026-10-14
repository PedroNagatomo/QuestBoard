package com.questboard.dto.loot;

import com.questboard.domain.enums.Rarity;
import jakarta.validation.constraints.*;

public record CreateLootRequest(
        @NotBlank @Size(max = 150) String name,
        String description,
        @Size(max = 500) String imageUrl,
        @NotNull @Min(1) @Max(1_000_000) Integer costCoins,
        @Min(0) Integer stock,
        Rarity rarity
) {}