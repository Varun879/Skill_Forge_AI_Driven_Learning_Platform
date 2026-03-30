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
    name = "learner_module_progress",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_learner_module_progress", columnNames = {"module_id", "learner_id"})
    },
    indexes = {
        @Index(name = "idx_learner_module_progress_learner", columnList = "learner_id"),
        @Index(name = "idx_learner_module_progress_module", columnList = "module_id")
    }
)
public class LearnerModuleProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private CourseModule module;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learner_id", nullable = false)
    private User learner;

    @CreationTimestamp
    @Column(name = "completed_at", nullable = false, updatable = false)
    private LocalDateTime completedAt;

    public Long getId() { return id; }
    public CourseModule getModule() { return module; }
    public User getLearner() { return learner; }
    public LocalDateTime getCompletedAt() { return completedAt; }

    public void setModule(CourseModule module) { this.module = module; }
    public void setLearner(User learner) { this.learner = learner; }
}
