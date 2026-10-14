package com.questboard.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Optional<CustomUserDetails> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return Optional.empty();
        if (auth.getPrincipal() instanceof CustomUserDetails cud) return Optional.of(cud);
        return Optional.empty();
    }

    public static UUID currentUserId() {
        return currentUser().map(CustomUserDetails::getId).orElse(null);
    }

    public static String currentUsername() {
        return currentUser().map(CustomUserDetails::getUsername).orElse(null);
    }
}