package com.skillforge.domain.courseexam.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillforge.domain.courseexam.entity.CourseExamAttemptAnswer;

public interface CourseExamAttemptAnswerRepository extends JpaRepository<CourseExamAttemptAnswer, Long> {

	void deleteByAttemptId(Long attemptId);
}
