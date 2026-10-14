package com.questboard.service;

import com.questboard.domain.User;
import com.questboard.domain.enums.PlayerClass;
import com.questboard.dto.auth.*;
import com.questboard.dto.user.UserResponse;
import com.questboard.exception.ConflictException;
import com.questboard.exception.UnauthorizedException;
import com.questboard.repository.UserRepository;
import com.questboard.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final LevelService levelService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new ConflictException("Username já está em uso");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new ConflictException("Email já está cadastrado");
        }

        User u = User.builder()
                .username(req.username())
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .displayName(req.displayName() != null ? req.displayName() : req.username())
                .playerClass(req.playerClass() != null ? req.playerClass() : PlayerClass.DEV)
                .role("USER")
                .level(1).xp(0).totalXp(0).coins(0)
                .currentStreak(0).longestStreak(0)
                .enabled(true)
                .build();
        userRepository.save(u);

        log.info("🆕 Novo usuário: {} ({})", u.getUsername(), u.getPlayerClass());

        String access  = jwtService.generateAccessToken(u.getId(), u.getUsername(), u.getRole());
        String refresh = jwtService.generateRefreshToken(u.getId(), u.getUsername());

        return AuthResponse.of(access, refresh, jwtService.getAccessExpirationMs(),
                UserResponse.from(u, levelService.xpToNextLevel(u.getLevel())));
    }

    public AuthResponse login(LoginRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        } catch (AuthenticationException e) {
            throw new UnauthorizedException("Usuário ou senha inválidos");
        }

        User u = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));

        String access  = jwtService.generateAccessToken(u.getId(), u.getUsername(), u.getRole());
        String refresh = jwtService.generateRefreshToken(u.getId(), u.getUsername());

        log.info("🔑 Login: {}", u.getUsername());
        return AuthResponse.of(access, refresh, jwtService.getAccessExpirationMs(),
                UserResponse.from(u, levelService.xpToNextLevel(u.getLevel())));
    }

    public AuthResponse refresh(RefreshRequest req) {
        String token = req.refreshToken();
        if (!jwtService.isValid(token) || !jwtService.isRefreshToken(token)) {
            throw new UnauthorizedException("Refresh token inválido");
        }
        var userId = jwtService.extractUserId(token);
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));

        String access  = jwtService.generateAccessToken(u.getId(), u.getUsername(), u.getRole());
        String refresh = jwtService.generateRefreshToken(u.getId(), u.getUsername());
        return AuthResponse.of(access, refresh, jwtService.getAccessExpirationMs(),
                UserResponse.from(u, levelService.xpToNextLevel(u.getLevel())));
    }
}