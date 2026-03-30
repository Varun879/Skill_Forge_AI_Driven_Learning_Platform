package com.skillforge.domain.problem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.skillforge.common.enums.DifficultyLevel;
import com.skillforge.domain.problem.entity.Problem;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {

    List<Problem> findAllByOrderByCreatedAtDesc();

    List<Problem> findByDifficultyLevelOrderByCreatedAtDesc(DifficultyLevel difficultyLevel);
}
