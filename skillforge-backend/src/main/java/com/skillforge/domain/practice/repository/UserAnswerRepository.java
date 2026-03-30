package com.skillforge.domain.practice.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.domain.practice.entity.UserAnswer;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {

        java.util.List<UserAnswer> findAllByOrderByCreatedAtDesc();

    long countByUserIdAndCreatedAtBetween(Long userId, LocalDateTime from, LocalDateTime to);

    long countByUserIdAndIsCorrectTrueAndCreatedAtBetween(Long userId, LocalDateTime from, LocalDateTime to);

    @Query("""
            SELECT COALESCE(AVG(ua.timeTakenSeconds), 0)
            FROM UserAnswer ua
            WHERE ua.user.id = :userId
              AND ua.createdAt BETWEEN :from AND :to
            """)
    Double averageTimeByUserBetween(
            @Param("userId") Long userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    long countByUserIdAndQuestionQuestionType(Long userId, PracticeQuestionType questionType);

    long countByUserIdAndIsCorrectTrueAndQuestionQuestionTypeIn(Long userId, Collection<PracticeQuestionType> types);

    @Query("""
            SELECT DISTINCT ua.question.id
            FROM UserAnswer ua
            WHERE ua.user.id = :userId
              AND ua.question.questionType IN :types
            ORDER BY ua.question.id
            """)
    List<Long> findDistinctQuestionIdsByUserIdAndQuestionTypeIn(
            @Param("userId") Long userId,
            @Param("types") Collection<PracticeQuestionType> types);
}
