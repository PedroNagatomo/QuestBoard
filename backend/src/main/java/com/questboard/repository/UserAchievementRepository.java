package com.questboard.repository;

import com.questboard.domain.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {

    @Query("""
        SELECT ua FROM UserAchievement ua
        JOIN FETCH ua.achievement
        WHERE ua.user.id = :userId
        """)
    List<UserAchievement> findByUserIdWithAchievement(@Param("userId") UUID userId);

    // --- métodos antigos mantidos ---
    List<UserAchievement> findByUserId(UUID userId);

    Optional<UserAchievement> findByUserIdAndAchievementId(UUID userId, UUID achievementId);
}