package com.questboard.integration.core;

import com.questboard.domain.Quest;
import com.questboard.domain.User;
import com.questboard.domain.enums.QuestStatus;
import com.questboard.repository.QuestRepository;
import com.questboard.service.QuestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntegrationService {

    private final QuestRepository questRepository;
    private final QuestService questService;
    private final ExternalQuestWriter writer;

    public void handle(ExternalEvent event) {
        log.info("🔌 [{}] {} — {} ({})",
                event.source(), event.type(), event.title(), event.externalId());

        switch (event.type()) {
            case ISSUE_CREATED -> writer.createFromExternal(event);
            case ISSUE_COMPLETED, PR_MERGED -> completeExternal(event);
            case ISSUE_UPDATED -> writer.updateFromExternal(event);
        }
    }

    private void completeExternal(ExternalEvent e) {
        Optional<Quest> existing = questRepository
                .findByExternalSourceAndExternalId(e.source().name(), e.externalId());
        if (existing.isEmpty()) {
            log.debug("Quest inexistente para {}#{} — ignorando",
                    e.source(), e.externalId());
            return;
        }

        Quest q = existing.get();
        if (q.getStatus() == QuestStatus.COMPLETED) return;

        User actor = q.getAssignee() != null ? q.getAssignee() : q.getCreatedBy();
        try {
            questService.complete(q.getId(), actor.getId(), null);
            log.info("✅ Quest {} concluída via {}", q.getTitle(), e.source());
        } catch (Exception ex) {
            log.warn("Falha ao concluir quest {} via {}: {}",
                    q.getId(), e.source(), ex.getMessage());
        }
    }
}