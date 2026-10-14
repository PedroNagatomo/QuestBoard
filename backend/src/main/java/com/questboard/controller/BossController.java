package com.questboard.controller;

import com.questboard.dto.boss.BossFightResponse;
import com.questboard.dto.boss.CreateBossRequest;
import com.questboard.dto.boss.DamageBossRequest;
import com.questboard.security.CustomUserDetails;
import com.questboard.service.BossFightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bosses")
@RequiredArgsConstructor
public class BossController {

    private final BossFightService bossService;

    @GetMapping
    public List<BossFightResponse> listActive() {
        return bossService.listActive();
    }

    @GetMapping("/guild/{guildId}")
    public List<BossFightResponse> listByGuild(@PathVariable UUID guildId) {
        return bossService.listByGuild(guildId);
    }

    @GetMapping("/{id}")
    public BossFightResponse get(@PathVariable UUID id) {
        return BossFightResponse.from(bossService.require(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BossFightResponse create(@AuthenticationPrincipal CustomUserDetails principal,
                                    @Valid @RequestBody CreateBossRequest req) {
        return bossService.create(principal.getId(), req);
    }

    @PostMapping("/{id}/damage")
    public BossFightResponse damage(@PathVariable UUID id,
                                    @Valid @RequestBody DamageBossRequest req) {
        return bossService.applyDamage(id, req);
    }
}