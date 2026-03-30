package com.skillforge.domain.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillforge.domain.chat.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByCourseIdAndTutorIdAndStudentId(Long courseId, Long tutorId, Long studentId);

    List<ChatRoom> findByTutorIdOrderByCreatedAtDesc(Long tutorId);

    List<ChatRoom> findByTutorIdAndCourseIdOrderByCreatedAtDesc(Long tutorId, Long courseId);
}
