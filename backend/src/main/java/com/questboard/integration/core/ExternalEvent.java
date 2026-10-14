package com.questboard.integration.core;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Evento padronizado vindo de qualquer integração externa.
 */
public record ExternalEvent(
        Source source,          // GITHUB, JIRA, TRELLO
        EventType type,         // ISSUE_CREATED, ISSUE_COMPLETED, PR_MERGED, ...
        String externalId,      // id único na ferramenta de origem
        String title,
        String description,
        String url,
        String difficultyHint,  // dica opcional (ex: label "bug", "critical")
        UUID guildId,           // guilda alvo (opcional)
        Map<String, Object> raw,
        Instant occurredAt
) {
    public enum Source { GITHUB, JIRA, TRELLO }
    public enum EventType { ISSUE_CREATED, ISSUE_COMPLETED, ISSUE_UPDATED, PR_MERGED }
}