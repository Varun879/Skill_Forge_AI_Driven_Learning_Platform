package com.skillforge.domain.exam.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillforge.domain.exam.entity.ExamSession;

public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {

    Optional<ExamSession> findTopByUserIdOrderByStartTimeDesc(Long userId);
}
