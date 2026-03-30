package com.skillforge.domain.practice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillforge.domain.practice.entity.PracticeSession;

public interface PracticeSessionRepository extends JpaRepository<PracticeSession, Long> {

    List<PracticeSession> findTop10ByUserIdOrderByStartedAtDesc(Long userId);

    Optional<PracticeSession> findByIdAndUserId(Long id, Long userId);
}
