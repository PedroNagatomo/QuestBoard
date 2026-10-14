package com.questboard.repository;

import com.questboard.domain.Quest;
import com.questboard.domain.enums.QuestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestRepository extends JpaRepository<Quest, UUID> {

    // ---------- Buscas com fetch eagerly das relações usadas no QuestResponse ----------

    @Query("""
        SELECT q FROM Quest q
        LEFT JOIN FETCH q.assignee
        LEFT JOIN FETCH q.createdBy
        LEFT JOIN FETCH q.guild
        LEFT JOIN FETCH q.parentBoss
        WHERE q.id = :id
        """)
    Optional<Quest> findByIdWithRelations(@Param("id") UUID id);

    @Query(value = """
        SELECT q FROM Quest q
        LEFT JOIN FETCH q.assignee
        LEFT JOIN FETCH q.createdBy
        LEFT JOIN FETCH q.guild
        LEFT JOIN FETCH q.parentBoss
        """,
            countQuery = "SELECT COUNT(q) FROM Quest q")
    Page<Quest> findAllWithRelations(Pageable pageable);

    @Query(value = """
        SELECT q FROM Quest q
        LEFT JOIN FETCH q.assignee
        LEFT JOIN FETCH q.createdBy
        LEFT JOIN FETCH q.guild
        LEFT JOIN FETCH q.parentBoss
        WHERE q.status = :status
        """,
            countQuery = "SELECT COUNT(q) FROM Quest q WHERE q.status = :status")
    Page<Quest> findByStatusWithRelations(@Param("status") QuestStatus status, Pageable pageable);

    @Query(value = """
        SELECT q FROM Quest q
        LEFT JOIN FETCH q.assignee
        LEFT JOIN FETCH q.createdBy
        LEFT JOIN FETCH q.guild
        LEFT JOIN FETCH q.parentBoss
        WHERE q.assignee.id = :assigneeId
        """,
            countQuery = "SELECT COUNT(q) FROM Quest q WHERE q.assignee.id = :assigneeId")
    Page<Quest> findByAssigneeIdWithRelations(@Param("assigneeId") UUID assigneeId, Pageable pageable);

    @Query(value = """
        SELECT q FROM Quest q
        LEFT JOIN FETCH q.assignee
        LEFT JOIN FETCH q.createdBy
        LEFT JOIN FETCH q.guild
        LEFT JOIN FETCH q.parentBoss
        WHERE q.guild.id = :guildId
        """,
            countQuery = "SELECT COUNT(q) FROM Quest q WHERE q.guild.id = :guildId")
    Page<Quest> findByGuildIdWithRelations(@Param("guildId") UUID guildId, Pageable pageable);

    // ---------- Mantidos (usados por outros services) ----------

    Page<Quest> findByAssigneeId(UUID assigneeId, Pageable pageable);

    Page<Quest> findByGuildId(UUID guildId, Pageable pageable);

    Page<Quest> findByStatus(QuestStatus status, Pageable pageable);

    Optional<Quest> findByExternalSourceAndExternalId(String source, String externalId);

    List<Quest> findByParentBossId(UUID bossId);

    long countByAssigneeIdAndStatus(UUID assigneeId, QuestStatus status);

    @Query("""
        SELECT COUNT(q) FROM Quest q
        WHERE q.assignee.id = :userId
          AND q.status = 'COMPLETED'
          AND q.completedAt >= :since
        """)
    long countCompletedSince(@Param("userId") UUID userId, @Param("since") Instant since);

    @Query("""
        SELECT COALESCE(SUM(q.xpReward), 0) FROM Quest q
        WHERE q.assignee.id = :userId
          AND q.status = 'COMPLETED'
          AND q.completedAt >= :since
        """)
    long sumXpEarnedSince(@Param("userId") UUID userId, @Param("since") Instant since);
}