package com.questboard.integration.trello;

import com.fasterxml.jackson.databind.JsonNode;
import com.questboard.integration.core.ExternalEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class TrelloEventParser {

    public ExternalEvent parse(JsonNode payload, UUID guildId) {
        String actionType = payload.path("action").path("type").asText();
        JsonNode data = payload.path("action").path("data");

        if ("createCard".equals(actionType)) {
            JsonNode card = data.path("card");
            return new ExternalEvent(
                    ExternalEvent.Source.TRELLO, ExternalEvent.EventType.ISSUE_CREATED,
                    "trello-" + card.path("id").asText(),
                    card.path("name").asText(),
                    card.path("desc").asText(""),
                    "https://trello.com/c/" + card.path("shortLink").asText(),
                    "medium", guildId, null, Instant.now()
            );
        }

        if ("updateCard".equals(actionType)) {
            JsonNode listAfter = data.path("listAfter");
            JsonNode card = data.path("card");
            String listName = listAfter.path("name").asText("").toLowerCase();
            boolean isDone = listName.contains("done") || listName.contains("conclu") || listName.contains("feito");
            if (!isDone) return null;

            return new ExternalEvent(
                    ExternalEvent.Source.TRELLO, ExternalEvent.EventType.ISSUE_COMPLETED,
                    "trello-" + card.path("id").asText(),
                    card.path("name").asText(),
                    card.path("desc").asText(""),
                    "https://trello.com/c/" + card.path("shortLink").asText(),
                    null, guildId, null, Instant.now()
            );
        }
        return null;
    }
}