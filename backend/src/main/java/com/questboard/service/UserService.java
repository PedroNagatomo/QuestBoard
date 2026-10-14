package com.questboard.service;

import com.questboard.domain.User;
import com.questboard.domain.UserAchievement;
import com.questboard.domain.UserBadge;
import com.questboard.dto.badge.BadgeResponse;
import com.questboard.dto.user.UpdateUserRequest;
import com.questboard.dto.user.UserProgressResponse;
import com.questboard.dto.user.UserResponse;
import com.questboard.exception.NotFoundException;
import com.questboard.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final QuestRepository questRepository;
    private final LevelService levelService;

    public User require(UUID id) {
        return userRepository.findByIdWithGuild(id)
                .orElseThrow(() -> new NotFoundException("User", id));
    }

    public User requireByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User", username));
    }

    public UserResponse toResponse(User u) {
        long xpToNext = levelService.xpToNextLevel(u.getLevel());
        return UserResponse.from(u, xpToNext);
    }

    public Page<UserResponse> list(String q, Pageable pageable) {
        return userRepository.search(q, pageable).map(this::toResponse);
    }

    @Transactional
    public UserResponse update(UUID userId, UpdateUserRequest req) {
        User u = require(userId);
        if (req.displayName() != null) u.setDisplayName(req.displayName());
        if (req.avatarUrl() != null)   u.setAvatarUrl(req.avatarUrl());
        if (req.playerClass() != null) u.setPlayerClass(req.playerClass());
        userRepository.save(u);
        return toResponse(u);
    }

    public UserProgressResponse progress(UUID userId) {
        User u = require(userId);


        long questsCompleted = questRepository.countByAssigneeIdAndStatus(
                u.getId(), com.questboard.domain.enums.QuestStatus.COMPLETED);

        List<UserBadge> badges = userBadgeRepository.findByUserIdWithBadge(userId);
        List<UserAchievement> achs = userAchievementRepository.findByUserIdWithAchievement(userId);

        long xpToNext = levelService.xpToNextLevel(u.getLevel());
        double pct = xpToNext == Long.MAX_VALUE
                ? 100.0
                : Math.min(100.0, (double) u.getXp() / xpToNext * 100.0);

        var badgeViews = badges.stream()
                .map(ub -> new UserProgressResponse.BadgeView(
                        ub.getBadge().getCode(), ub.getBadge().getName(),
                        ub.getBadge().getIconUrl(), ub.getBadge().getRarity().name()))
                .toList();

        var achViews = achs.stream()
                .map(ua -> new UserProgressResponse.AchievementView(
                        ua.getAchievement().getCode(), ua.getAchievement().getName(),
                        ua.getProgress(), ua.getAchievement().getTargetValue(),
                        ua.isCompleted()))
                .toList();

        return new UserProgressResponse(
                u.getId(), u.getLevel(), u.getXp(), xpToNext,
                Math.round(pct * 100.0) / 100.0,
                u.getTotalXp(), u.getCoins(),
                u.getCurrentStreak(), u.getLongestStreak(),
                questsCompleted, 0L,
                badgeViews, achViews
        );
    }

    public List<BadgeResponse> listBadges(UUID userId) {
        return userBadgeRepository.findByUserIdWithBadge(userId).stream()
                .map(ub -> BadgeResponse.from(ub.getBadge(), ub.getAwardedAt()))
                .toList();
    }
}