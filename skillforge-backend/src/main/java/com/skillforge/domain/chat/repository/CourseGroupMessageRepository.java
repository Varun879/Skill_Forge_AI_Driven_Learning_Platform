package com.skillforge.domain.chat.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.skillforge.domain.chat.entity.CourseGroupMessage;

public interface CourseGroupMessageRepository extends JpaRepository<CourseGroupMessage, Long> {

    Page<CourseGroupMessage> findByCourseIdOrderByMessageTimeDesc(Long courseId, Pageable pageable);
}
