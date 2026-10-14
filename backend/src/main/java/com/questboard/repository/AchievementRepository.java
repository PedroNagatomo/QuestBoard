package com.questboard.repository;

import com.questboard.domain.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AchievementRepository extends JpaRepository<Achievement, UUID> {
    Optional<Achievement> findByCode(String code);
    List<Achievement> findByMetric(String metric);
}