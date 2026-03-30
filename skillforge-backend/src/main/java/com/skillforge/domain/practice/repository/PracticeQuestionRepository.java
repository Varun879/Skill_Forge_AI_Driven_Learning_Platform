package com.skillforge.domain.practice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.skillforge.common.enums.DifficultyLevel;
import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.domain.practice.entity.PracticeQuestion;

public interface PracticeQuestionRepository extends JpaRepository<PracticeQuestion, Long> {

    @Query("""
            SELECT q FROM PracticeQuestion q
            WHERE q.isActive = true
              AND (:type IS NULL OR q.questionType = :type)
              AND (:difficulty IS NULL OR q.difficultyLevel = :difficulty)
                                                        AND (:topic IS NULL OR LOWER(q.topic) = LOWER(CAST(:topic AS string)))
            ORDER BY q.createdAt DESC
            """)
    List<PracticeQuestion> findFiltered(
            @Param("type") PracticeQuestionType type,
            @Param("difficulty") DifficultyLevel difficulty,
            @Param("topic") String topic);

    @Query("""
            SELECT q FROM PracticeQuestion q
            WHERE q.isActive = true
              AND (:type IS NULL OR q.questionType = :type)
                                                        AND (:topic IS NULL OR LOWER(q.topic) = LOWER(CAST(:topic AS string)))
              AND q.id NOT IN (
                    SELECT ua.question.id
                    FROM UserAnswer ua
                    WHERE ua.user.id = :userId
              )
            ORDER BY q.successRate ASC, q.estimatedSolveTimeMinutes ASC, q.createdAt DESC
            """)
    List<PracticeQuestion> findRecommendedUnattempted(
            @Param("userId") Long userId,
            @Param("type") PracticeQuestionType type,
            @Param("topic") String topic,
            Pageable pageable);

    @Query("""
            SELECT q FROM PracticeQuestion q
            WHERE q.isActive = true
              AND (:type IS NULL OR q.questionType = :type)
                                                        AND (:topic IS NULL OR LOWER(q.topic) = LOWER(CAST(:topic AS string)))
            ORDER BY q.successRate ASC, q.estimatedSolveTimeMinutes ASC, q.createdAt DESC
            """)
    List<PracticeQuestion> findFallbackRecommendation(
            @Param("type") PracticeQuestionType type,
            @Param("topic") String topic,
            Pageable pageable);

    @Query("""
            SELECT DISTINCT q FROM PracticeQuestion q
            LEFT JOIN q.tags tag
            WHERE q.isActive = true
              AND q.questionType = com.skillforge.common.enums.PracticeQuestionType.PROGRAMMING_MCQ
              AND (:difficulty IS NULL OR q.difficultyLevel = :difficulty)
                                                        AND (:topic IS NULL OR LOWER(q.topic) = LOWER(CAST(:topic AS string)))
                                                        AND (:tag IS NULL OR LOWER(tag) = LOWER(CAST(:tag AS string)))
            ORDER BY q.createdAt DESC
            """)
    List<PracticeQuestion> findProgrammingMcqQuestions(
            @Param("difficulty") DifficultyLevel difficulty,
            @Param("topic") String topic,
            @Param("tag") String tag,
            Pageable pageable);

    @Query("""
            SELECT DISTINCT q FROM PracticeQuestion q
            LEFT JOIN q.tags tag
            WHERE q.isActive = true
              AND q.questionType = com.skillforge.common.enums.PracticeQuestionType.APTITUDE_MCQ
              AND (:difficulty IS NULL OR q.difficultyLevel = :difficulty)
                                                        AND (:topic IS NULL OR LOWER(q.topic) = LOWER(CAST(:topic AS string)))
                                                        AND (:tag IS NULL OR LOWER(tag) = LOWER(CAST(:tag AS string)))
            ORDER BY q.createdAt DESC
            """)
    List<PracticeQuestion> findAptitudeQuestions(
            @Param("difficulty") DifficultyLevel difficulty,
            @Param("topic") String topic,
            @Param("tag") String tag,
            Pageable pageable);

    List<PracticeQuestion> findByIdInAndQuestionTypeInAndIsActiveTrue(
            java.util.Collection<Long> ids,
            java.util.Collection<PracticeQuestionType> questionTypes);

    List<PracticeQuestion> findByTopicIgnoreCaseAndQuestionTypeInAndIsActiveTrue(
            String topic,
            java.util.Collection<PracticeQuestionType> questionTypes);

    List<PracticeQuestion> findByQuestionTypeInAndIsActiveTrue(
            java.util.Collection<PracticeQuestionType> questionTypes);

    Optional<PracticeQuestion> findByIdAndIsActiveTrue(Long id);
}
