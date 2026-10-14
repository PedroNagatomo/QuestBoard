package com.questboard.dto.user;

import com.questboard.domain.User;
import com.questboard.domain.enums.PlayerClass;

import java.util.UUID;

public record UserSummary(
        UUID id,
        String username,
        String displayName,
        String avatarUrl,
        PlayerClass playerClass,
        int level,
        long totalXp
) {
    public static UserSummary from(User u) {
        return new UserSummary(
                u.getId(), u.getUsername(), u.getDisplayName(),
                u.getAvatarUrl(), u.getPlayerClass(), u.getLevel(), u.getTotalXp()
        );
    }
}