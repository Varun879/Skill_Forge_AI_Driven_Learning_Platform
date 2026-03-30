package com.skillforge.domain.practice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.domain.practice.entity.PracticeStats;

public interface PracticeStatsRepository extends JpaRepository<PracticeStats, Long> {

    List<PracticeStats> findByUserIdOrderByQuestionTypeAsc(Long userId);

    Optional<PracticeStats> findByUserIdAndQuestionType(Long userId, PracticeQuestionType questionType);
}
