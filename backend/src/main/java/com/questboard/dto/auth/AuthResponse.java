package com.questboard.dto.auth;

import com.questboard.dto.user.UserResponse;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
    public static AuthResponse of(String access, String refresh, long expiresInMs, UserResponse user) {
        return new AuthResponse(access, refresh, "Bearer", expiresInMs / 1000, user);
    }
}