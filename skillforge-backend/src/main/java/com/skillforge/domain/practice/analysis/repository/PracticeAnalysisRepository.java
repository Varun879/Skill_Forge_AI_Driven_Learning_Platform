package com.skillforge.domain.practice.analysis.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.skillforge.common.enums.DifficultyLevel;
import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.domain.practice.entity.PracticeQuestion;
import com.skillforge.domain.practice.entity.UserAnswer;

/**
 * Dedicated read-only query surface for the Practice analysis sub-module.
 * This repository is separate from (and does not replace) the existing
 * UserAnswerRepository — Spring Data JPA supports multiple repositories
 * targeting the same entity.
 *
 * All queries are read-only; no writes are performed here.
 */
public interface PracticeAnalysisRepository extends JpaRepository<UserAnswer, Long> {

    // -------------------------------------------------------------------------
    // UserAnswer queries
    // -------------------------------------------------------------------------

    /**
     * Returns the most-recent answers submitted by a user for a specific
     * topic and question type, newest first.
     * Use a {@code Pageable} of {@code PageRequest.of(0, n)} to bound results.
     */
    @Query("""
            SELECT ua FROM UserAnswer ua
            WHERE ua.user.id = :userId
              AND LOWER(ua.question.topic) = LOWER(:topic)
              AND ua.question.questionType = :type
            ORDER BY ua.createdAt DESC
            """)
    List<UserAnswer> findRecentAnswersByUserTopicAndType(
            @Param("userId") Long userId,
            @Param("topic") String topic,
            @Param("type") PracticeQuestionType type,
            Pageable pageable);

    /**
     * Returns the distinct set of topics that a user has already attempted
     * for a specific question type.
     */
    @Query("""
            SELECT DISTINCT ua.question.topic
            FROM UserAnswer ua
            WHERE ua.user.id = :userId
              AND ua.question.questionType = :type
            """)
    List<String> findAttemptedTopicsByUserAndType(
            @Param("userId") Long userId,
            @Param("type") PracticeQuestionType type);

    // -------------------------------------------------------------------------
    // PracticeQuestion queries
    // -------------------------------------------------------------------------

    /**
     * Returns the average estimated solve time (in minutes) for active questions
     * in a given topic and question type. Returns {@code null} when no questions
     * exist for that combination.
     */
    @Query("""
            SELECT AVG(CAST(pq.estimatedSolveTimeMinutes AS double))
            FROM PracticeQuestion pq
            WHERE LOWER(pq.topic) = LOWER(:topic)
              AND pq.questionType = :type
              AND pq.isActive = true
            """)
    Double findAvgEstimatedSolveTimeMinutesForTopic(
            @Param("topic") String topic,
            @Param("type") PracticeQuestionType type);

    /**
     * Counts active questions available for the given question type.
     * Used by {@link com.skillforge.domain.practice.analysis.PracticeQuestionService}
     * to check whether generation is needed before serving a question.
     */
    @Query("""
            SELECT COUNT(pq) FROM PracticeQuestion pq
            WHERE pq.questionType = :type
              AND pq.isActive = true
            """)
    long countActiveQuestionsByType(@Param("type") PracticeQuestionType type);

    /**
     * Returns all distinct topics that have at least one active question
     * of the given type. Used by {@link com.skillforge.domain.practice.analysis.NextQuestionSelector}
     * to identify topics not yet attempted by the user.
     */
    @Query("""
            SELECT DISTINCT pq.topic
            FROM PracticeQuestion pq
            WHERE pq.questionType = :type
              AND pq.isActive = true
            """)
    List<String> findAllActiveTopicsByType(@Param("type") PracticeQuestionType type);

    /**
     * Fetches active questions for a specific type, topic, and difficulty level
     * that the user has NOT yet attempted.
     * Results are ordered by ascending success-rate (harder / less-solved first)
     * then by creation date.
     */
    @Query("""
            SELECT pq FROM PracticeQuestion pq
            WHERE pq.isActive = true
              AND pq.questionType = :type
              AND LOWER(pq.topic) = LOWER(:topic)
              AND pq.difficultyLevel = :difficulty
              AND pq.id NOT IN (
                    SELECT ua.question.id
                    FROM UserAnswer ua
                    WHERE ua.user.id = :userId
              )
            ORDER BY pq.successRate ASC, pq.createdAt DESC
            """)
    List<PracticeQuestion> findUnattemptedQuestionsForUserTopicDifficulty(
            @Param("userId") Long userId,
            @Param("type") PracticeQuestionType type,
            @Param("topic") String topic,
            @Param("difficulty") DifficultyLevel difficulty,
            Pageable pageable);
}
