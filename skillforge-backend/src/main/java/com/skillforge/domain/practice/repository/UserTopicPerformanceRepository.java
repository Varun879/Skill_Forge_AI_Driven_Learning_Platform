package com.skillforge.domain.practice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.domain.practice.entity.UserTopicPerformance;

/**
 * Spring Data JPA repository for {@link UserTopicPerformance}.
 * All writes come exclusively from
 * {@link com.skillforge.domain.practice.recommendation.service.UserPerformanceAnalyzer}.
 */
public interface UserTopicPerformanceRepository extends JpaRepository<UserTopicPerformance, Long> {

    List<UserTopicPerformance> findByUserIdAndQuestionTypeOrderByAccuracyAsc(
            Long userId, PracticeQuestionType questionType);

    Optional<UserTopicPerformance> findByUserIdAndCategoryIgnoreCaseAndQuestionType(
            Long userId, String category, PracticeQuestionType questionType);
}
