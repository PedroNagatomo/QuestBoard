package com.questboard.controller;

import com.questboard.dto.auth.AuthResponse;
import com.questboard.dto.auth.LoginRequest;
import com.questboard.dto.auth.RefreshRequest;
import com.questboard.dto.auth.RegisterRequest;
import com.questboard.dto.user.UserResponse;
import com.questboard.security.CustomUserDetails;
import com.questboard.service.AuthService;
import com.questboard.service.LevelService;
import com.questboard.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final LevelService levelService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return authService.refresh(req);
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal CustomUserDetails principal) {
        var u = userService.require(principal.getId());
        return userService.toResponse(u);
    }
}