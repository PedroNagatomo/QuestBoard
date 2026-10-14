package com.questboard.integration.slack;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SlackNotifier {

    private final RestClient http = RestClient.create();

    @Value("${app.integrations.slack.webhook-url:}")
    private String webhookUrl;

    @Value("${app.integrations.slack.enabled:false}")
    private boolean enabled;

    public void notify(String text, String emoji, List<Map<String, Object>> blocks) {
        if (!enabled || webhookUrl == null || webhookUrl.isBlank()) {
            log.debug("Slack desabilitado — notificação descartada: {}", text);
            return;
        }
        try {
            http.post()
                    .uri(webhookUrl)
                    .body(Map.of("text", emoji + " " + text, "blocks", blocks))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Falha ao notificar Slack: {}", e.getMessage());
        }
    }

    public void notifyBossDefeated(String bossName) {
        notify(bossName + " foi derrotado!",
                "💀",
                List.of(Map.of(
                        "type", "section",
                        "text", Map.of("type", "mrkdwn",
                                "text", "*💀 Boss derrotado:* " + bossName + "\nA guilda ganhou XP!")
                )));
    }

    public void notifyLevelUp(String username, int newLevel) {
        notify(username + " subiu para o nível " + newLevel + "!",
                "⬆️",
                List.of(Map.of(
                        "type", "section",
                        "text", Map.of("type", "mrkdwn",
                                "text", "*⬆️ Level Up!* @" + username + " agora é nível *" + newLevel + "*")
                )));
    }

    public void notifyQuestCompleted(String username, String questTitle, int xp) {
        notify(username + " completou: " + questTitle,
                "⚔️",
                List.of(Map.of(
                        "type", "section",
                        "text", Map.of("type", "mrkdwn",
                                "text", "*⚔️ Quest completa:* " + questTitle +
                                        "\n_por_ @" + username + " _(+" + xp + " XP)_")
                )));
    }
}