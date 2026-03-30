package com.skillforge.domain.course.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.skillforge.domain.course.entity.CourseTag;

@Repository
public interface CourseTagRepository extends JpaRepository<CourseTag, Long> {
}
