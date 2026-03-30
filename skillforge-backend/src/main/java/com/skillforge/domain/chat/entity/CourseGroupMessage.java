package com.skillforge.domain.chat.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.skillforge.domain.course.entity.Course;
import com.skillforge.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "course_group_messages",
    indexes = {
        @Index(name = "idx_course_group_messages_course_time", columnList = "course_id,message_time"),
        @Index(name = "idx_course_group_messages_sender", columnList = "sender_id")
    }
)
public class CourseGroupMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_role", nullable = false, length = 20)
    private ChatSenderRole senderRole;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @CreationTimestamp
    @Column(name = "message_time", nullable = false, updatable = false)
    private LocalDateTime messageTime;

    public Long getId() { return id; }
    public Course getCourse() { return course; }
    public User getSender() { return sender; }
    public ChatSenderRole getSenderRole() { return senderRole; }
    public String getMessage() { return message; }
    public LocalDateTime getMessageTime() { return messageTime; }

    public void setCourse(Course course) { this.course = course; }
    public void setSender(User sender) { this.sender = sender; }
    public void setSenderRole(ChatSenderRole senderRole) { this.senderRole = senderRole; }
    public void setMessage(String message) { this.message = message; }
}
