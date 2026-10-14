package com.questboard.integration.jira;

import com.fasterxml.jackson.databind.JsonNode;
import com.questboard.integration.core.ExternalEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class JiraEventParser {

    public ExternalEvent parse(JsonNode payload, UUID guildId) {
        String webhookEvent = payload.path("webhookEvent").asText("");

        JsonNode issue = payload.path("issue");
        if (issue.isMissingNode()) return null;

        String key = issue.path("key").asText();
        JsonNode fields = issue.path("fields");
        String summary = fields.path("summary").asText();
        String description = fields.path("description").asText("");
        String status = fields.path("status").path("name").asText("");
        String priority = fields.path("priority").path("name").asText("");
        String url = "https://your-domain.atlassian.net/browse/" + key;

        ExternalEvent.EventType type;
        if (webhookEvent.startsWith("jira:issue_created")) {
            type = ExternalEvent.EventType.ISSUE_CREATED;
        } else if (webhookEvent.startsWith("jira:issue_updated")) {
            boolean done = "Done".equalsIgnoreCase(status) || "Concluído".equalsIgnoreCase(status);
            type = done ? ExternalEvent.EventType.ISSUE_COMPLETED : ExternalEvent.EventType.ISSUE_UPDATED;
        } else {
            return null;
        }

        return new ExternalEvent(
                ExternalEvent.Source.JIRA, type,
                "jira-" + key, summary, description, url,
                priority, guildId,
                null, Instant.now()
        );
    }
}