package com.questboard.integration.trello;

import com.fasterxml.jackson.databind.JsonNode;
import com.questboard.integration.core.ExternalEvent;
import com.questboard.integration.core.IntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/integrations/trello")
@RequiredArgsConstructor
@Slf4j
public class TrelloWebhookController {

    private final TrelloEventParser parser;
    private final IntegrationService integrationService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestParam(value = "guildId", required = false) UUID guildId,
            @RequestBody JsonNode payload) {

        ExternalEvent event = parser.parse(payload, guildId);
        if (event != null) integrationService.handle(event);
        return ResponseEntity.ok().build();
    }
}