package com.questboard.integration.core;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/integrations")
@RequiredArgsConstructor
public class WebhookController {

    private final IntegrationService integrationService;

    /**
     * DTO tipado em Java 17 — record padrão (sem features de 21+).
     */
    public record TestEventRequest(
            @NotNull ExternalEvent.Source source,
            @NotNull ExternalEvent.EventType type,
            @NotBlank String externalId,
            @NotBlank String title,
            String description,
            String url,
            String difficultyHint,
            UUID guildId
    ) {}

    @PostMapping(value = "/test", consumes = "application/json;charset=UTF-8")
    public ResponseEntity<Void> test(@Valid @RequestBody TestEventRequest req) {
        ExternalEvent event = new ExternalEvent(
                req.source(),
                req.type(),
                req.externalId(),
                req.title(),
                req.description(),
                req.url(),
                req.difficultyHint(),
                req.guildId(),
                null,
                Instant.now()
        );
        integrationService.handle(event);
        return ResponseEntity.ok().build();
    }
}