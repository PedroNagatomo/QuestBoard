package com.questboard.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPublisher {

    private final SimpMessagingTemplate messaging;

    /** Envia um payload para o tópico público do leaderboard. */
    public void publishLeaderboardUpdate(String period, Object payload) {
        String destination = "/topic/leaderboard/" + period;
        messaging.convertAndSend(destination, payload);
        log.debug("📡 WS → {} ({})", destination, payload.getClass().getSimpleName());
    }

    /** Notificação pessoal (aparece só para aquele usuário). */
    public void notifyUser(UUID userId, String type, Map<String, Object> data) {
        String destination = "/queue/notifications";
        try {
            messaging.convertAndSendToUser(
                    userId.toString(),
                    destination,
                    Map.of("type", type, "payload", data, "ts", System.currentTimeMillis())
            );
        } catch (Exception e) {
            log.warn("Falha ao notificar usuário {}: {}", userId, e.getMessage());
        }
    }
}