package com.questboard.dto.guild;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGuildRequest(
        @NotBlank @Size(min = 3, max = 100) String name,
        @Size(max = 1000) String description,
        @Size(max = 500) String emblemUrl
) {}