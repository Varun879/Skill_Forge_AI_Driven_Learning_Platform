package com.skillforge.domain.course.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.skillforge.domain.course.entity.CourseEnrollment;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {

    boolean existsByCourseIdAndLearnerId(Long courseId, Long learnerId);

    boolean existsByCourseIdAndLearnerIdAndCourseTutorId(Long courseId, Long learnerId, Long tutorId);

    List<CourseEnrollment> findByLearnerId(Long learnerId);
}
