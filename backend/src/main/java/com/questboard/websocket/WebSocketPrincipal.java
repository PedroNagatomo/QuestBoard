package com.questboard.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.security.Principal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class WebSocketPrincipal implements Principal {
    private final UUID userId;
    private final String username;
    private final String role;

    @Override
    public String getName() {
        return username;
    }
}