package com.questboard.integration.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.questboard.integration.core.ExternalEvent;
import com.questboard.integration.core.IntegrationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/integrations/github")
@RequiredArgsConstructor
@Slf4j
public class GitHubWebhookController {

    private final GitHubSignatureVerifier verifier;
    private final GitHubEventParser parser;
    private final IntegrationService integrationService;
    private final ObjectMapper mapper;

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String sig,
            @RequestHeader(value = "X-GitHub-Event", required = false) String eventType,
            @RequestParam(value = "guildId", required = false) UUID guildId,
            HttpServletRequest req) throws IOException {

        String body = new String(req.getInputStream().readAllBytes());
        if (!verifier.verify(body, sig)) {
            log.warn("GitHub webhook com assinatura inválida");
            return ResponseEntity.status(401).build();
        }
        if (eventType == null) return ResponseEntity.badRequest().build();

        JsonNode payload = mapper.readTree(body);
        ExternalEvent event = parser.parse(eventType, payload, guildId);
        if (event != null) integrationService.handle(event);

        return ResponseEntity.ok().build();
    }
}