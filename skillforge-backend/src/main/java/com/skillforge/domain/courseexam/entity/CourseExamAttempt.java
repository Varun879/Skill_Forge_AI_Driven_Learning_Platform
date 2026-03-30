package com.skillforge.domain.courseexam.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
    name = "course_exam_attempts",
    indexes = {
        @Index(name = "idx_course_exam_attempts_exam", columnList = "course_exam_id"),
        @Index(name = "idx_course_exam_attempts_learner", columnList = "learner_id")
    }
)
public class CourseExamAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_exam_id", nullable = false)
    private CourseExam courseExam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learner_id", nullable = false)
    private User learner;

    @CreationTimestamp
    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CourseExamAttemptStatus status = CourseExamAttemptStatus.STARTED;

    public Long getId() { return id; }
    public CourseExam getCourseExam() { return courseExam; }
    public User getLearner() { return learner; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public BigDecimal getScore() { return score; }
    public CourseExamAttemptStatus getStatus() { return status; }

    public void setCourseExam(CourseExam courseExam) { this.courseExam = courseExam; }
    public void setLearner(User learner) { this.learner = learner; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public void setScore(BigDecimal score) { this.score = score; }
    public void setStatus(CourseExamAttemptStatus status) { this.status = status; }
}
