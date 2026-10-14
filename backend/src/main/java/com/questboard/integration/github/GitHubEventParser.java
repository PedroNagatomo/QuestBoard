package com.questboard.integration.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.questboard.integration.core.ExternalEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class GitHubEventParser {

    /**
     * @param eventType header X-GitHub-Event ("issues", "pull_request")
     */
    public ExternalEvent parse(String eventType, JsonNode payload, UUID guildId) {
        return switch (eventType) {
            case "issues" -> parseIssue(payload, guildId);
            case "pull_request" -> parsePullRequest(payload, guildId);
            default -> null;
        };
    }

    private ExternalEvent parseIssue(JsonNode p, UUID guildId) {
        String action = p.path("action").asText();
        JsonNode issue = p.path("issue");
        String id = issue.path("number").asText();
        String title = issue.path("title").asText();
        String body = issue.path("body").asText("");
        String url = issue.path("html_url").asText();
        String state = issue.path("state").asText();

        ExternalEvent.EventType type = switch (action) {
            case "opened"   -> ExternalEvent.EventType.ISSUE_CREATED;
            case "closed"   -> ExternalEvent.EventType.ISSUE_COMPLETED;
            case "edited", "reopened" -> ExternalEvent.EventType.ISSUE_UPDATED;
            default -> null;
        };
        if (type == null) return null;

        // dica de dificuldade: pega labels
        StringBuilder hint = new StringBuilder();
        issue.path("labels").forEach(l -> hint.append(l.path("name").asText()).append(","));

        return new ExternalEvent(
                ExternalEvent.Source.GITHUB, type,
                "issue-" + id, title, body, url,
                hint.toString(), guildId,
                null, Instant.now()
        );
    }

    private ExternalEvent parsePullRequest(JsonNode p, UUID guildId) {
        String action = p.path("action").asText();
        if (!"closed".equals(action)) return null;

        JsonNode pr = p.path("pull_request");
        boolean merged = pr.path("merged").asBoolean(false);
        if (!merged) return null;

        String id = pr.path("number").asText();
        String title = pr.path("title").asText();
        String body = pr.path("body").asText("");
        String url = pr.path("html_url").asText();

        return new ExternalEvent(
                ExternalEvent.Source.GITHUB, ExternalEvent.EventType.PR_MERGED,
                "pr-" + id, "Merge: " + title, body, url,
                "high", guildId,
                null, Instant.now()
        );
    }
}