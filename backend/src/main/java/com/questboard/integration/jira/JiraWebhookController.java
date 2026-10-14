package com.questboard.integration.jira;

import com.fasterxml.jackson.databind.JsonNode;
import com.questboard.integration.core.ExternalEvent;
import com.questboard.integration.core.IntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/integrations/jira")
@RequiredArgsConstructor
@Slf4j
public class JiraWebhookController {

    private final JiraEventParser parser;
    private final IntegrationService integrationService;

    @Value("${app.integrations.jira.webhook-token}")
    private String expectedToken;

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestParam(value = "token", required = false) String token,
            @RequestParam(value = "guildId", required = false) UUID guildId,
            @RequestBody JsonNode payload) {

        if (!expectedToken.equals(token)) {
            log.warn("Jira webhook com token inválido");
            return ResponseEntity.status(401).build();
        }

        ExternalEvent event = parser.parse(payload, guildId);
        if (event != null) integrationService.handle(event);
        return ResponseEntity.ok().build();
    }
}