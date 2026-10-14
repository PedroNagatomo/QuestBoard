package com.questboard.service;

import com.questboard.domain.Guild;
import com.questboard.domain.User;
import com.questboard.dto.guild.CreateGuildRequest;
import com.questboard.dto.guild.GuildResponse;
import com.questboard.dto.guild.UpdateGuildRequest;
import com.questboard.dto.user.UserSummary;
import com.questboard.exception.BadRequestException;
import com.questboard.exception.ConflictException;
import com.questboard.exception.ForbiddenException;
import com.questboard.exception.NotFoundException;
import com.questboard.repository.GuildRepository;
import com.questboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuildService {

    private final GuildRepository guildRepository;
    private final UserRepository userRepository;

    public Guild require(UUID id) {
        return guildRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Guild", id));
    }

    @Transactional
    public GuildResponse create(UUID ownerId, CreateGuildRequest req) {
        if (guildRepository.existsByName(req.name())) {
            throw new ConflictException("Já existe uma guilda com esse nome");
        }
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User", ownerId));

        Guild g = Guild.builder()
                .name(req.name())
                .description(req.description())
                .emblemUrl(req.emblemUrl())
                .ownerId(owner.getId())
                .build();
        guildRepository.save(g);

        // Cria automaticamente o dono na guilda
        owner.setGuild(g);
        userRepository.save(owner);

        log.info("🏰 Guilda criada: {} por {}", g.getName(), owner.getUsername());
        return GuildResponse.from(g);
    }

    public GuildResponse get(UUID id) { return GuildResponse.from(require(id)); }

    public Page<GuildResponse> list(Pageable pageable) {
        return guildRepository.findAll(pageable).map(GuildResponse::from);
    }

    public List<UserSummary> members(UUID guildId) {
        require(guildId);
        return userRepository.findByGuildOrderByTotalXp(guildId).stream()
                .map(UserSummary::from)
                .toList();
    }

    @Transactional
    public GuildResponse update(UUID guildId, UUID actorId, UpdateGuildRequest req) {
        Guild g = require(guildId);
        if (!g.getOwnerId().equals(actorId)) {
            throw new ForbiddenException("Apenas o dono pode editar a guilda");
        }
        if (req.description() != null) g.setDescription(req.description());
        if (req.emblemUrl()   != null) g.setEmblemUrl(req.emblemUrl());
        guildRepository.save(g);
        return GuildResponse.from(g);
    }

    @Transactional
    public void join(UUID guildId, UUID userId) {
        Guild g = require(guildId);
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));
        if (u.getGuild() != null) {
            throw new BadRequestException("Você já está em uma guilda. Saia antes de entrar em outra.");
        }
        u.setGuild(g);
        userRepository.save(u);
        log.info("➕ {} entrou na guilda {}", u.getUsername(), g.getName());
    }

    @Transactional
    public void leave(UUID userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));
        if (u.getGuild() == null) {
            throw new BadRequestException("Você não está em nenhuma guilda");
        }
        if (u.getGuild().getOwnerId().equals(userId)) {
            throw new BadRequestException("O dono não pode sair. Transfira a posse ou exclua a guilda.");
        }
        u.setGuild(null);
        userRepository.save(u);
    }

    @Transactional
    public void addXp(UUID guildId, long xp) {
        if (xp <= 0) return;
        Guild g = guildRepository.findById(guildId).orElse(null);
        if (g == null) return;
        g.setTotalXp(g.getTotalXp() + xp);
        // Nível da guilda cresce a cada 10.000 XP
        g.setLevel((int) (g.getTotalXp() / 10_000) + 1);
        guildRepository.save(g);
    }
}