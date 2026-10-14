package com.questboard.websocket;

import com.questboard.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String auth = accessor.getFirstNativeHeader("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                log.debug("WS CONNECT sem Authorization — permitindo como anônimo");
                return message;
            }
            String token = auth.substring(7);
            if (!jwtService.isValid(token) || !jwtService.isAccessToken(token)) {
                log.warn("WS CONNECT com JWT inválido");
                return message;
            }
            try {
                WebSocketPrincipal principal = new WebSocketPrincipal(
                        jwtService.extractUserId(token),
                        jwtService.extractUsername(token),
                        jwtService.extractRole(token)
                );
                accessor.setUser(principal);
                log.debug("WS autenticado: {}", principal.getUsername());
            } catch (Exception e) {
                log.warn("Falha ao autenticar WS: {}", e.getMessage());
            }
        }
        return message;
    }
}