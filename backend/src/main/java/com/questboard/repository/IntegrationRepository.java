package com.questboard.repository;

import com.questboard.domain.Integration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IntegrationRepository extends JpaRepository<Integration, UUID> {

    Optional<Integration> findByProviderAndGuildId(String provider, UUID guildId);

    List<Integration> findByGuildIdAndActiveTrue(UUID guildId);

    List<Integration> findByActiveTrue();
}