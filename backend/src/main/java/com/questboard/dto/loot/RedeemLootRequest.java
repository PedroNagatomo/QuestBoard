package com.questboard.dto.loot;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RedeemLootRequest(@NotNull UUID lootId) {}