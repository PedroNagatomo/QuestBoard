package com.questboard.controller;

import com.questboard.domain.LootRedemption;
import com.questboard.dto.loot.CreateLootRequest;
import com.questboard.dto.loot.LootItemResponse;
import com.questboard.dto.loot.RedeemLootRequest;
import com.questboard.security.CustomUserDetails;
import com.questboard.service.LootService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LootController {

    private final LootService lootService;

    @GetMapping("/marketplace")
    public Page<LootItemResponse> catalog(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return lootService.catalog(PageRequest.of(page, Math.min(size, 100)));
    }

    @PostMapping("/marketplace")
    @ResponseStatus(HttpStatus.CREATED)
    public LootItemResponse createItem(@Valid @RequestBody CreateLootRequest req) {
        return lootService.create(req);
    }

    @PostMapping("/marketplace/redeem")
    public LootRedemption redeem(@AuthenticationPrincipal CustomUserDetails principal,
                                 @Valid @RequestBody RedeemLootRequest req) {
        return lootService.redeem(principal.getId(), req.lootId());
    }

    @GetMapping("/me/redemptions")
    public Page<LootRedemption> myRedemptions(@AuthenticationPrincipal CustomUserDetails principal,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return lootService.myRedemptions(principal.getId(),
                PageRequest.of(page, Math.min(size, 100)));
    }

    @PostMapping("/admin/redemptions/{id}/approve")
    public LootRedemption approve(@PathVariable UUID id,
                                  @AuthenticationPrincipal CustomUserDetails principal) {
        return lootService.updateStatus(id, LootRedemption.Status.APPROVED, principal.getId());
    }

    @PostMapping("/admin/redemptions/{id}/deliver")
    public LootRedemption deliver(@PathVariable UUID id,
                                  @AuthenticationPrincipal CustomUserDetails principal) {
        return lootService.updateStatus(id, LootRedemption.Status.DELIVERED, principal.getId());
    }

    @PostMapping("/admin/redemptions/{id}/reject")
    public LootRedemption reject(@PathVariable UUID id,
                                 @AuthenticationPrincipal CustomUserDetails principal) {
        return lootService.updateStatus(id, LootRedemption.Status.REJECTED, principal.getId());
    }
}