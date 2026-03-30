package com.skillforge.domain.practice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.skillforge.domain.practice.entity.SessionQuestion;

public interface SessionQuestionRepository extends JpaRepository<SessionQuestion, Long> {

    long countBySessionId(Long sessionId);

    long countBySessionIdAndIsCorrectTrue(Long sessionId);

    @Query("""
            SELECT COALESCE(SUM(sq.timeTakenSeconds), 0)
            FROM SessionQuestion sq
            WHERE sq.session.id = :sessionId
            """)
    Long sumTimeTakenBySessionId(@Param("sessionId") Long sessionId);

    @Query("""
            SELECT COUNT(DISTINCT sq.question.topic)
            FROM SessionQuestion sq
            WHERE sq.session.id = :sessionId
            """)
    long countDistinctTopicsBySessionId(@Param("sessionId") Long sessionId);

    List<SessionQuestion> findTop10BySessionIdOrderByCreatedAtDesc(Long sessionId);
}
