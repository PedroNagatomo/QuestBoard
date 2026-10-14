package com.questboard.dto.guild;

import jakarta.validation.constraints.Size;

public record UpdateGuildRequest(
        @Size(max = 1000) String description,
        @Size(max = 500) String emblemUrl
) {}