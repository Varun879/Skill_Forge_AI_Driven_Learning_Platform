package com.skillforge.domain.practice.recommendation.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.skillforge.common.enums.DifficultyLevel;
import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.domain.practice.entity.PracticeQuestion;

/**
 * Read-only query surface for the recommendation sub-module.
 * Targets {@link PracticeQuestion} directly; does NOT conflict with the
 * existing {@code PracticeQuestionRepository} — Spring Data JPA allows
 * multiple repositories per entity.
 */
public interface RecommendationQuestionRepository extends JpaRepository<PracticeQuestion, Long> {

    /**
     * Counts active questions for a specific category (topic) and question type.
     * Used to decide whether AI generation is needed.
     */
    @Query("""
            SELECT COUNT(pq) FROM PracticeQuestion pq
            WHERE LOWER(pq.topic) = LOWER(:category)
              AND pq.questionType = :type
              AND pq.isActive = true
            """)
    long countByCategoryAndType(
            @Param("category") String category,
            @Param("type") PracticeQuestionType type);

    /**
     * Returns active, unattempted questions for the user matching the given
     * category, type, and difficulty.  Ordered by ascending success-rate so
     * harder (less solved) questions surface first.
     */
    @Query("""
            SELECT pq FROM PracticeQuestion pq
            WHERE pq.isActive = true
              AND pq.questionType = :type
              AND LOWER(pq.topic) = LOWER(:category)
              AND pq.difficultyLevel = :difficulty
              AND pq.id NOT IN (
                    SELECT ua.question.id FROM UserAnswer ua WHERE ua.user.id = :userId
              )
            ORDER BY pq.successRate ASC, pq.createdAt DESC
            """)
    List<PracticeQuestion> findUnattemptedByCategoryAndDifficulty(
            @Param("userId") Long userId,
            @Param("type") PracticeQuestionType type,
            @Param("category") String category,
            @Param("difficulty") DifficultyLevel difficulty,
            Pageable pageable);

    /**
     * Fallback: returns any active, unattempted question for the given type
     * regardless of category or difficulty.
     */
    @Query("""
            SELECT pq FROM PracticeQuestion pq
            WHERE pq.isActive = true
              AND pq.questionType = :type
              AND pq.id NOT IN (
                    SELECT ua.question.id FROM UserAnswer ua WHERE ua.user.id = :userId
              )
            ORDER BY pq.successRate ASC, pq.createdAt DESC
            """)
    List<PracticeQuestion> findAnyUnattempted(
            @Param("userId") Long userId,
            @Param("type") PracticeQuestionType type,
            Pageable pageable);

    @Query("""
            SELECT pq FROM PracticeQuestion pq
            WHERE pq.isActive = true
              AND pq.questionType = :type
              AND LOWER(pq.topic) = LOWER(:category)
              AND pq.id NOT IN (
                    SELECT ua.question.id FROM UserAnswer ua WHERE ua.user.id = :userId
              )
            ORDER BY pq.successRate ASC, pq.createdAt DESC
            """)
    List<PracticeQuestion> findAnyUnattemptedByCategory(
            @Param("userId") Long userId,
            @Param("type") PracticeQuestionType type,
            @Param("category") String category,
            Pageable pageable);

    /**
     * Global fallback: returns any active question for the given type
     * (including already-attempted ones).
     */
    @Query("""
            SELECT pq FROM PracticeQuestion pq
            WHERE pq.isActive = true
              AND pq.questionType = :type
            ORDER BY pq.successRate ASC, pq.createdAt DESC
            """)
    List<PracticeQuestion> findAnyActive(
            @Param("type") PracticeQuestionType type,
            Pageable pageable);

    @Query("""
            SELECT pq FROM PracticeQuestion pq
            WHERE pq.isActive = true
              AND pq.questionType = :type
              AND LOWER(pq.topic) = LOWER(:category)
            ORDER BY pq.successRate ASC, pq.createdAt DESC
            """)
    List<PracticeQuestion> findAnyActiveByCategory(
            @Param("type") PracticeQuestionType type,
            @Param("category") String category,
            Pageable pageable);

    /**
     * Returns the most recent answer's question for the user in a given
     * category and type — used to infer the current difficulty level.
     */
    @Query("""
            SELECT ua.question FROM UserAnswer ua
            WHERE ua.user.id = :userId
              AND ua.question.questionType = :type
              AND LOWER(ua.question.topic) = LOWER(:category)
            ORDER BY ua.createdAt DESC
            """)
    List<PracticeQuestion> findLastAttemptedQuestionInCategory(
            @Param("userId") Long userId,
            @Param("type") PracticeQuestionType type,
            @Param("category") String category,
            Pageable pageable);

    /**
     * Returns the most recent user answers (isCorrect + timeTaken) for a
     * given category and type — used to check consecutive-failure streak.
     */
    @Query("""
            SELECT ua.isCorrect FROM UserAnswer ua
            WHERE ua.user.id = :userId
              AND ua.question.questionType = :type
              AND LOWER(ua.question.topic) = LOWER(:category)
            ORDER BY ua.createdAt DESC
            """)
    List<Boolean> findRecentIsCorrectByCategoryAndType(
            @Param("userId") Long userId,
            @Param("type") PracticeQuestionType type,
            @Param("category") String category,
            Pageable pageable);

    /**
     * Returns the average estimated solve time (minutes) across active questions
     * in a category/type combination.
     */
    @Query("""
            SELECT AVG(CAST(pq.estimatedSolveTimeMinutes AS double))
            FROM PracticeQuestion pq
            WHERE LOWER(pq.topic) = LOWER(:category)
              AND pq.questionType = :type
              AND pq.isActive = true
            """)
    Double findAvgEstimatedSolveTimeMinutes(
            @Param("category") String category,
            @Param("type") PracticeQuestionType type);
}
