package com.skillforge.domain.exam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillforge.domain.exam.entity.ExamQuestion;

public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {

    List<ExamQuestion> findByExamSessionIdOrderByQuestionOrderAsc(Long examSessionId);
}
