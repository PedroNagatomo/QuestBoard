package com.questboard.service;

import com.questboard.domain.LootItem;
import com.questboard.domain.LootRedemption;
import com.questboard.domain.User;
import com.questboard.dto.loot.CreateLootRequest;
import com.questboard.dto.loot.LootItemResponse;
import com.questboard.domain.enums.Rarity;
import com.questboard.exception.BadRequestException;
import com.questboard.exception.ForbiddenException;
import com.questboard.exception.NotFoundException;
import com.questboard.repository.LootItemRepository;
import com.questboard.repository.LootRedemptionRepository;
import com.questboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LootService {

    private final LootItemRepository lootRepository;
    private final LootRedemptionRepository redemptionRepository;
    private final UserRepository userRepository;

    public LootItem require(UUID id) {
        return lootRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("LootItem", id));
    }

    public LootItemResponse create(CreateLootRequest req) {
        LootItem l = LootItem.builder()
                .name(req.name())
                .description(req.description())
                .imageUrl(req.imageUrl())
                .costCoins(req.costCoins())
                .stock(req.stock())
                .rarity(req.rarity() != null ? req.rarity() : Rarity.COMMON)
                .active(true)
                .build();
        lootRepository.save(l);
        return LootItemResponse.from(l);
    }

    public Page<LootItemResponse> catalog(Pageable pageable) {
        return lootRepository.findByActiveTrue(pageable).map(LootItemResponse::from);
    }

    /**
     * Resgata um item: debita coins, decrementa stock (se limitado),
     * cria LootRedemption com status PENDING (aguardando aprovação do admin).
     */
    @Transactional
    public LootRedemption redeem(UUID userId, UUID lootId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));
        LootItem l = require(lootId);

        if (!l.isActive()) throw new BadRequestException("Item inativo");
        if (l.getStock() != null && l.getStock() <= 0) {
            throw new BadRequestException("Item sem estoque");
        }
        if (u.getCoins() < l.getCostCoins()) {
            throw new BadRequestException("Coins insuficientes");
        }

        u.setCoins(u.getCoins() - l.getCostCoins());
        if (l.getStock() != null) l.setStock(l.getStock() - 1);

        userRepository.save(u);
        lootRepository.save(l);

        LootRedemption r = LootRedemption.builder()
                .user(u)
                .loot(l)
                .costPaid(l.getCostCoins())
                .status(LootRedemption.Status.PENDING)
                .build();
        redemptionRepository.save(r);

        log.info("🎁 {} resgatou {} por {} coins", u.getUsername(), l.getName(), l.getCostCoins());
        return r;
    }

    public Page<LootRedemption> myRedemptions(UUID userId, Pageable pageable) {
        return redemptionRepository.findByUserId(userId, pageable);
    }

    @Transactional
    public LootRedemption updateStatus(UUID redemptionId, LootRedemption.Status newStatus, UUID actorId) {
        LootRedemption r = redemptionRepository.findById(redemptionId)
                .orElseThrow(() -> new NotFoundException("LootRedemption", redemptionId));

        // Apenas admin (aqui simplificado: o próprio ator é "admin" — na versão final checar role)
        if (r.getStatus() == LootRedemption.Status.DELIVERED
                || r.getStatus() == LootRedemption.Status.REJECTED) {
            throw new BadRequestException("Redenção já finalizada");
        }

        // Se rejeitar, devolve coins e estoque
        if (newStatus == LootRedemption.Status.REJECTED) {
            User u = r.getUser();
            u.setCoins(u.getCoins() + r.getCostPaid());
            userRepository.save(u);
            LootItem l = r.getLoot();
            if (l.getStock() != null) {
                l.setStock(l.getStock() + 1);
                lootRepository.save(l);
            }
        }

        r.setStatus(newStatus);
        redemptionRepository.save(r);
        return r;
    }
}