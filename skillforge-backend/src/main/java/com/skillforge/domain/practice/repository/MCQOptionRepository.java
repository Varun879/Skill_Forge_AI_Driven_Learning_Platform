package com.skillforge.domain.practice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillforge.domain.practice.entity.MCQOption;

public interface MCQOptionRepository extends JpaRepository<MCQOption, Long> {

    List<MCQOption> findByQuestionIdOrderByDisplayOrderAsc(Long questionId);
}
