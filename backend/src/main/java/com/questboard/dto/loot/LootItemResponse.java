package com.questboard.dto.loot;

import com.questboard.domain.LootItem;

import java.util.UUID;

public record LootItemResponse(
        UUID id,
        String name,
        String description,
        String imageUrl,
        int costCoins,
        Integer stock,
        String rarity,
        boolean active
) {
    public static LootItemResponse from(LootItem l) {
        return new LootItemResponse(l.getId(), l.getName(), l.getDescription(), l.getImageUrl(),
                l.getCostCoins(), l.getStock(), l.getRarity().name(), l.isActive());
    }
}