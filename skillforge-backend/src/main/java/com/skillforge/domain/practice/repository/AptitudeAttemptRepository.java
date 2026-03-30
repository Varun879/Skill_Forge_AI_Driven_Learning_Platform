package com.skillforge.domain.practice.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.skillforge.domain.practice.entity.AptitudeAttempt;

public interface AptitudeAttemptRepository extends JpaRepository<AptitudeAttempt, Long> {

    @Query("""
            SELECT a
            FROM AptitudeAttempt a
            WHERE a.user.id = :userId
              AND a.question.questionType = com.skillforge.common.enums.PracticeQuestionType.APTITUDE_MCQ
            ORDER BY a.createdAt DESC
            """)
    List<AptitudeAttempt> findLatestAttempts(
            @Param("userId") Long userId,
            Pageable pageable);

    @Query("""
            SELECT COUNT(a)
            FROM AptitudeAttempt a
            WHERE a.user.id = :userId
              AND a.question.questionType = com.skillforge.common.enums.PracticeQuestionType.APTITUDE_MCQ
            """)
    long countAttempts(@Param("userId") Long userId);

    @Query("""
            SELECT COUNT(a)
            FROM AptitudeAttempt a
            WHERE a.user.id = :userId
              AND a.question.questionType = com.skillforge.common.enums.PracticeQuestionType.APTITUDE_MCQ
              AND a.isCorrect = true
            """)
    long countCorrectAttempts(@Param("userId") Long userId);

    @Query("""
            SELECT COALESCE(AVG(a.timeTakenSeconds), 0)
            FROM AptitudeAttempt a
            WHERE a.user.id = :userId
              AND a.question.questionType = com.skillforge.common.enums.PracticeQuestionType.APTITUDE_MCQ
            """)
    Double averageAttemptTime(@Param("userId") Long userId);
}
