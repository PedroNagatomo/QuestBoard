package com.questboard.repository;

import com.questboard.domain.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserBadgeRepository extends JpaRepository<UserBadge, UUID> {

    @Query("""
        SELECT ub FROM UserBadge ub
        JOIN FETCH ub.badge
        WHERE ub.user.id = :userId
        """)
    List<UserBadge> findByUserIdWithBadge(@Param("userId") UUID userId);

    // --- métodos antigos mantidos ---
    List<UserBadge> findByUserId(UUID userId);

    boolean existsByUserIdAndBadgeId(UUID userId, UUID badgeId);

    long countByUserId(UUID userId);
}