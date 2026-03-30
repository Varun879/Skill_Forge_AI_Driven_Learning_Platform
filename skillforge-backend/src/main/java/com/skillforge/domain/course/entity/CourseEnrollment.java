package com.skillforge.domain.course.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
    name = "course_enrollments",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_enrollment_course_learner",
            columnNames = {"course_id", "learner_id"}
        )
    },
    indexes = {
        @Index(name = "idx_enrollments_course_id",  columnList = "course_id"),
        @Index(name = "idx_enrollments_learner_id", columnList = "learner_id")
    }
)
public class CourseEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learner_id", nullable = false)
    private User learner;

    @CreationTimestamp
    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private LocalDateTime enrolledAt;

    protected CourseEnrollment() {}

    public CourseEnrollment(Course course, User learner) {
        this.course  = course;
        this.learner = learner;
    }

    public Long getId()                { return id; }
    public Course getCourse()          { return course; }
    public User getLearner()           { return learner; }
    public LocalDateTime getEnrolledAt(){ return enrolledAt; }
}
