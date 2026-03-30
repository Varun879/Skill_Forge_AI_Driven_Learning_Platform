package com.skillforge.domain.execution.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillforge.domain.execution.entity.ExecutionTestResult;

public interface ExecutionTestResultRepository extends JpaRepository<ExecutionTestResult, Long> {
    List<ExecutionTestResult> findByExecutionIdOrderByIdAsc(Long executionId);
}
