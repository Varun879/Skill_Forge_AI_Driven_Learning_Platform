package com.skillforge.domain.chat.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.skillforge.domain.course.entity.Course;
import com.skillforge.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "chat_rooms",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_chat_room_course_tutor_student",
            columnNames = {"course_id", "tutor_id", "student_id"}
        )
    },
    indexes = {
        @Index(name = "idx_chat_rooms_course_id", columnList = "course_id"),
        @Index(name = "idx_chat_rooms_tutor_id", columnList = "tutor_id"),
        @Index(name = "idx_chat_rooms_student_id", columnList = "student_id")
    }
)
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tutor_id", nullable = false)
    private User tutor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public Course getCourse() {
        return course;
    }

    public User getTutor() {
        return tutor;
    }

    public User getStudent() {
        return student;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public void setTutor(User tutor) {
        this.tutor = tutor;
    }

    public void setStudent(User student) {
        this.student = student;
    }
}
