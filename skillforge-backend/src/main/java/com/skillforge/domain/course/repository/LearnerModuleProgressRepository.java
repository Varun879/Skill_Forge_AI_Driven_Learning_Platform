package com.skillforge.domain.course.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillforge.domain.course.entity.LearnerModuleProgress;

public interface LearnerModuleProgressRepository extends JpaRepository<LearnerModuleProgress, Long> {

    boolean existsByModuleIdAndLearnerId(Long moduleId, Long learnerId);

    long countByModuleCourseIdAndLearnerId(Long courseId, Long learnerId);

    List<LearnerModuleProgress> findByModuleCourseIdAndLearnerId(Long courseId, Long learnerId);
}
