package com.questboard.controller;

import com.questboard.dto.badge.BadgeResponse;
import com.questboard.dto.user.UpdateUserRequest;
import com.questboard.dto.user.UserProgressResponse;
import com.questboard.dto.user.UserResponse;
import com.questboard.security.CustomUserDetails;
import com.questboard.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** Lista pública paginada de jogadores (para ranking, diretório, etc). */
    @GetMapping
    public Page<UserResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return userService.list(q, PageRequest.of(page, Math.min(size, 100)));
    }

    // ============ Rotas AUTENTICADAS (o "me") ============

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal CustomUserDetails principal) {
        return userService.toResponse(userService.require(principal.getId()));
    }

    @PatchMapping("/me")
    public UserResponse updateMe(@AuthenticationPrincipal CustomUserDetails principal,
                                 @Valid @RequestBody UpdateUserRequest req) {
        return userService.update(principal.getId(), req);
    }

    @GetMapping("/me/progress")
    public UserProgressResponse myProgress(@AuthenticationPrincipal CustomUserDetails principal) {
        return userService.progress(principal.getId());
    }

    @GetMapping("/me/badges")
    public List<BadgeResponse> myBadges(@AuthenticationPrincipal CustomUserDetails principal) {
        return userService.listBadges(principal.getId());
    }

    // ============ Rotas PÚBLICAS (por id) ============

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable UUID id) {
        return userService.toResponse(userService.require(id));
    }

    @GetMapping("/{id}/progress")
    public UserProgressResponse progressOf(@PathVariable UUID id) {
        return userService.progress(id);
    }
}