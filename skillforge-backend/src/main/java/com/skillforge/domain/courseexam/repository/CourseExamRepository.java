package com.skillforge.domain.courseexam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillforge.domain.courseexam.entity.CourseExam;

public interface CourseExamRepository extends JpaRepository<CourseExam, Long> {

    List<CourseExam> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    List<CourseExam> findByCourseIdAndIsPublishedTrueOrderByCreatedAtDesc(Long courseId);
}
