package com.skillforge.domain.practice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.domain.practice.entity.TopicMastery;

public interface TopicMasteryRepository extends JpaRepository<TopicMastery, Long> {

    List<TopicMastery> findByUserIdOrderByMasteryScoreAsc(Long userId);

    Optional<TopicMastery> findByUserIdAndTopicIgnoreCaseAndQuestionType(
            Long userId,
            String topic,
            PracticeQuestionType questionType);
}
