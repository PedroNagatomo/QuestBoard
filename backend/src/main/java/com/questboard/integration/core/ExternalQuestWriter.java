package com.questboard.integration.core;

import com.questboard.domain.Quest;
import com.questboard.domain.User;
import com.questboard.domain.enums.Difficulty;
import com.questboard.domain.enums.QuestStatus;
import com.questboard.exception.NotFoundException;
import com.questboard.repository.QuestRepository;
import com.questboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalQuestWriter {

    private final QuestRepository questRepository;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createFromExternal(ExternalEvent e) {
        Optional<Quest> existing = questRepository
                .findByExternalSourceAndExternalId(e.source().name(), e.externalId());
        if (existing.isPresent()) {
            log.debug("Quest já existe para {}#{}", e.source(), e.externalId());
            return;
        }

        User creator = userRepository.findByUsername("admin")
                .orElseThrow(() -> new NotFoundException("User admin"));

        Difficulty difficulty = guessDifficulty(e.difficultyHint());

        Quest q = Quest.builder()
                .title(e.title())
                .description(e.description())
                .difficulty(difficulty)
                .status(QuestStatus.OPEN)
                .xpReward(difficulty.getBaseXp())
                .coinReward(difficulty.getBaseCoins())
                .externalSource(e.source().name())
                .externalId(e.externalId())
                .externalUrl(e.url())
                .createdBy(creator)
                .build();

        questRepository.save(q);
        log.info("✨ Quest criada de {}: {} (dificuldade {})",
                e.source(), e.title(), difficulty);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateFromExternal(ExternalEvent e) {
        questRepository.findByExternalSourceAndExternalId(e.source().name(), e.externalId())
                .ifPresent(q -> {
                    if (e.title() != null && !e.title().isBlank()) q.setTitle(e.title());
                    if (e.description() != null) q.setDescription(e.description());
                    questRepository.save(q);
                });
    }

    private Difficulty guessDifficulty(String hint) {
        if (hint == null) return Difficulty.EASY;
        String h = hint.toLowerCase();
        if (h.contains("critical") || h.contains("urgent")) return Difficulty.LEGENDARY;
        if (h.contains("high") || h.contains("bug"))        return Difficulty.HARD;
        if (h.contains("medium") || h.contains("feature"))  return Difficulty.MEDIUM;
        if (h.contains("low") || h.contains("chore"))       return Difficulty.TRIVIAL;
        return Difficulty.EASY;
    }
}