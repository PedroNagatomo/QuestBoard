package com.questboard.repository;

import com.questboard.domain.Guild;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GuildRepository extends JpaRepository<Guild, UUID> {

    Optional<Guild> findByName(String name);

    boolean existsByName(String name);

    Page<Guild> findAllByOrderByTotalXpDesc(Pageable pageable);

    List<Guild> findTop50ByOrderByTotalXpDesc();
}