package com.skillforge.domain.practice.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.skillforge.domain.practice.entity.MCQAttempt;

public interface MCQAttemptRepository extends JpaRepository<MCQAttempt, Long> {

    @Query("""
            SELECT a
            FROM MCQAttempt a
            WHERE a.user.id = :userId
              AND a.question.questionType = com.skillforge.common.enums.PracticeQuestionType.PROGRAMMING_MCQ
            ORDER BY a.createdAt DESC
            """)
    List<MCQAttempt> findLatestProgrammingAttempts(
            @Param("userId") Long userId,
            Pageable pageable);

    @Query("""
            SELECT COUNT(a)
            FROM MCQAttempt a
            WHERE a.user.id = :userId
              AND a.question.questionType = com.skillforge.common.enums.PracticeQuestionType.PROGRAMMING_MCQ
            """)
    long countProgrammingAttempts(@Param("userId") Long userId);

    @Query("""
            SELECT COUNT(a)
            FROM MCQAttempt a
            WHERE a.user.id = :userId
              AND a.question.questionType = com.skillforge.common.enums.PracticeQuestionType.PROGRAMMING_MCQ
              AND a.isCorrect = true
            """)
    long countCorrectProgrammingAttempts(@Param("userId") Long userId);

    @Query("""
            SELECT COALESCE(AVG(a.timeTakenSeconds), 0)
            FROM MCQAttempt a
            WHERE a.user.id = :userId
              AND a.question.questionType = com.skillforge.common.enums.PracticeQuestionType.PROGRAMMING_MCQ
            """)
    Double averageProgrammingAttemptTime(@Param("userId") Long userId);
}
