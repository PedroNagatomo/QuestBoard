package com.questboard.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisWebSocketBridge {

    public static final String CHANNEL_LEADERBOARD = "ws:leaderboard";
    public static final String CHANNEL_USER        = "ws:user";

    private final StringRedisTemplate redis;
    private final RedisMessageListenerContainer listenerContainer;
    private final NotificationPublisher localPublisher;
    private final ObjectMapper mapper;

    /** Publica atualização de leaderboard para TODAS as instâncias. */
    public void broadcastLeaderboard(String period, Map<String, Object> payload) {
        try {
            String json = mapper.writeValueAsString(Map.of("period", period, "payload", payload));
            redis.convertAndSend(CHANNEL_LEADERBOARD, json);
        } catch (Exception e) {
            log.warn("Falha ao publicar leaderboard no Redis: {}", e.getMessage());
            // fallback: entrega local
            localPublisher.publishLeaderboardUpdate(period, payload);
        }
    }

    /** Notificação pessoal. Também passa pelo Redis para multi-instância. */
    public void notifyUser(UUID userId, String type, Map<String, Object> data) {
        try {
            String json = mapper.writeValueAsString(Map.of(
                    "userId", userId.toString(),
                    "type", type,
                    "payload", data
            ));
            redis.convertAndSend(CHANNEL_USER, json);
        } catch (Exception e) {
            log.warn("Falha ao publicar notificação no Redis: {}", e.getMessage());
            localPublisher.notifyUser(userId, type, data);
        }
    }

    @PostConstruct
    void subscribe() {
        listenerContainer.addMessageListener(leaderboardListener(), new ChannelTopic(CHANNEL_LEADERBOARD));
        listenerContainer.addMessageListener(userListener(), new ChannelTopic(CHANNEL_USER));
        log.info("🔌 RedisWebSocketBridge inscrita em {} e {}", CHANNEL_LEADERBOARD, CHANNEL_USER);
    }

    private MessageListener leaderboardListener() {
        return (Message msg, byte[] pattern) -> {
            try {
                Map<String, Object> parsed = mapper.readValue(msg.getBody(), Map.class);
                String period = (String) parsed.get("period");
                Object payload = parsed.get("payload");
                localPublisher.publishLeaderboardUpdate(period, payload);
            } catch (Exception e) {
                log.warn("Erro processando mensagem de leaderboard: {}", e.getMessage());
            }
        };
    }

    private MessageListener userListener() {
        return (Message msg, byte[] pattern) -> {
            try {
                Map<String, Object> parsed = mapper.readValue(msg.getBody(), Map.class);
                UUID userId = UUID.fromString((String) parsed.get("userId"));
                String type = (String) parsed.get("type");
                @SuppressWarnings("unchecked")
                Map<String, Object> payload = (Map<String, Object>) parsed.get("payload");
                localPublisher.notifyUser(userId, type, payload);
            } catch (Exception e) {
                log.warn("Erro processando mensagem de usuário: {}", e.getMessage());
            }
        };
    }
}