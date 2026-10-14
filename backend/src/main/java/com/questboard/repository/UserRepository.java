package com.questboard.repository;

import com.questboard.domain.User;
import com.questboard.domain.enums.PlayerClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("""
        SELECT u FROM User u
        WHERE (:q IS NULL OR
               LOWER(CAST(u.username AS string))    LIKE LOWER(CONCAT('%', CAST(:q AS string), '%')) OR
               LOWER(CAST(u.displayName AS string)) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%')))
        """)
    Page<User> search(@Param("q") String q, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.guild.id = :guildId ORDER BY u.totalXp DESC")
    List<User> findByGuildOrderByTotalXp(@Param("guildId") UUID guildId);

    /** Ranking global — ordenado por totalXp. */
    Page<User> findAllByOrderByTotalXpDesc(Pageable pageable);

    /** Ranking filtrado por classe. */
    Page<User> findByPlayerClassOrderByTotalXpDesc(PlayerClass playerClass, Pageable pageable);

    /** Top N simples (usado por leaderboard no Redis como fallback). */
    List<User> findTop100ByOrderByTotalXpDesc();

    @Query("""
        SELECT u FROM User u
        LEFT JOIN FETCH u.guild
        WHERE u.id = :id
        """)
    Optional<User> findByIdWithGuild(@Param("id") UUID id);
}