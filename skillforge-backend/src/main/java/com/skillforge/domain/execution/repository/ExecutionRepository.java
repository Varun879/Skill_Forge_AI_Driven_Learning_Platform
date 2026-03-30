package com.skillforge.domain.execution.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillforge.domain.execution.entity.Execution;

public interface ExecutionRepository extends JpaRepository<Execution, Long> {
    List<Execution> findByLearnerIdOrderByCreatedAtDesc(Long learnerId);
}
