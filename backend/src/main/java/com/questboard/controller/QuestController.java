package com.questboard.controller;

import com.questboard.domain.User;
import com.questboard.domain.enums.QuestStatus;
import com.questboard.dto.quest.*;
import com.questboard.security.CustomUserDetails;
import com.questboard.service.QuestService;
import com.questboard.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/quests")
@RequiredArgsConstructor
public class QuestController {

    private final QuestService questService;
    private final UserService userService;

    @GetMapping
    public Page<QuestResponse> list(@RequestParam(required = false) QuestStatus status,
                                    @RequestParam(required = false) UUID assigneeId,
                                    @RequestParam(required = false) UUID guildId,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return questService.list(status, assigneeId, guildId, PageRequest.of(page, Math.min(size, 100)));
    }

    @GetMapping("/{id}")
    public QuestResponse get(@PathVariable UUID id) { return questService.get(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuestResponse create(@AuthenticationPrincipal CustomUserDetails principal,
                                @Valid @RequestBody CreateQuestRequest req) {
        return questService.create(principal.getId(), req);
    }

    @PatchMapping("/{id}")
    public QuestResponse update(@PathVariable UUID id,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                @Valid @RequestBody UpdateQuestRequest req) {
        return questService.update(id, principal.getId(), req);
    }

    @PostMapping("/{id}/assign")
    public QuestResponse assign(@PathVariable UUID id,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                @RequestParam UUID assigneeId) {
        return questService.assign(id, principal.getId(), assigneeId);
    }

    @PostMapping("/{id}/start")
    public QuestResponse start(@PathVariable UUID id,
                               @AuthenticationPrincipal CustomUserDetails principal) {
        return questService.start(id, principal.getId());
    }

    @PostMapping("/{id}/complete")
    public QuestResponse complete(@PathVariable UUID id,
                                  @AuthenticationPrincipal CustomUserDetails principal,
                                  @RequestBody(required = false) CompleteQuestRequest req) {
        return questService.complete(id, principal.getId(), req);
    }

    @PostMapping("/{id}/cancel")
    public QuestResponse cancel(@PathVariable UUID id,
                                @AuthenticationPrincipal CustomUserDetails principal) {
        return questService.cancel(id, principal.getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id,
                       @AuthenticationPrincipal CustomUserDetails principal) {
        questService.delete(id, principal.getId());
    }
}