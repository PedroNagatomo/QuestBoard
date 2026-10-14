package com.questboard.dto.boss;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DamageBossRequest(
        @NotNull @Min(1) Long damage
) {}