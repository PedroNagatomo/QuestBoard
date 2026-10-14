package com.questboard.controller;

import com.questboard.dto.guild.CreateGuildRequest;
import com.questboard.dto.guild.GuildResponse;
import com.questboard.dto.guild.UpdateGuildRequest;
import com.questboard.dto.user.UserSummary;
import com.questboard.security.CustomUserDetails;
import com.questboard.service.GuildService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/guilds")
@RequiredArgsConstructor
public class GuildController {

    private final GuildService guildService;

    @GetMapping
    public Page<GuildResponse> list(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return guildService.list(PageRequest.of(page, Math.min(size, 100)));
    }

    @GetMapping("/{id}")
    public GuildResponse get(@PathVariable UUID id) {
        return guildService.get(id);
    }

    @GetMapping("/{id}/members")
    public List<UserSummary> members(@PathVariable UUID id) {
        return guildService.members(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GuildResponse create(@AuthenticationPrincipal CustomUserDetails principal,
                                @Valid @RequestBody CreateGuildRequest req) {
        return guildService.create(principal.getId(), req);
    }

    @PatchMapping("/{id}")
    public GuildResponse update(@PathVariable UUID id,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                @Valid @RequestBody UpdateGuildRequest req) {
        return guildService.update(id, principal.getId(), req);
    }

    @PostMapping("/{id}/join")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void join(@PathVariable UUID id,
                     @AuthenticationPrincipal CustomUserDetails principal) {
        guildService.join(id, principal.getId());
    }

    @PostMapping("/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leave(@AuthenticationPrincipal CustomUserDetails principal) {
        guildService.leave(principal.getId());
    }
}