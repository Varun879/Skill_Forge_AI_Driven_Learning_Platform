package com.skillforge.domain.courseexam.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillforge.domain.courseexam.entity.CourseExamAttempt;
import com.skillforge.domain.courseexam.entity.CourseExamAttemptStatus;

public interface CourseExamAttemptRepository extends JpaRepository<CourseExamAttempt, Long> {

    Optional<CourseExamAttempt> findTopByCourseExamIdAndLearnerIdAndStatusOrderByStartedAtDesc(
            Long courseExamId,
            Long learnerId,
            CourseExamAttemptStatus status);
}
