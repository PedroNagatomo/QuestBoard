package com.questboard.repository;

import com.questboard.domain.XpEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface XpEventRepository extends JpaRepository<XpEvent, UUID> {

    List<XpEvent> findTop50ByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query("""
        SELECT COALESCE(SUM(e.xpDelta), 0) FROM XpEvent e
        WHERE e.user.id = :userId
          AND e.createdAt >= :since
          AND e.xpDelta > 0
        """)
    long sumPositiveXpSince(@Param("userId") UUID userId, @Param("since") Instant since);

    long countByUserIdAndCreatedAtAfter(UUID userId, Instant since);

    long countByUserIdAndFlaggedTrue(UUID userId);
}