package com.skillforge.domain.practice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.skillforge.common.enums.PracticeQuestionType;
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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "practice_stats",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_practice_stats_user_type", columnNames = {"user_id", "question_type"})
    },
    indexes = {
        @Index(name = "idx_practice_stats_user_id", columnList = "user_id"),
        @Index(name = "idx_practice_stats_accuracy_rate", columnList = "accuracy_rate")
    }
)
public class PracticeStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 30)
    private PracticeQuestionType questionType;

    @Column(name = "total_attempted", nullable = false)
    private Integer totalAttempted = 0;

    @Column(name = "total_correct", nullable = false)
    private Integer totalCorrect = 0;

    @Column(name = "total_time_taken_seconds", nullable = false)
    private Long totalTimeTakenSeconds = 0L;

    @Column(name = "accuracy_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal accuracyRate = BigDecimal.ZERO;

    @Column(name = "last_answered_at")
    private LocalDateTime lastAnsweredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PracticeStats() {}

    public Long getId() { return id; }
    public User getUser() { return user; }
    public PracticeQuestionType getQuestionType() { return questionType; }
    public Integer getTotalAttempted() { return totalAttempted; }
    public Integer getTotalCorrect() { return totalCorrect; }
    public Long getTotalTimeTakenSeconds() { return totalTimeTakenSeconds; }
    public BigDecimal getAccuracyRate() { return accuracyRate; }
    public LocalDateTime getLastAnsweredAt() { return lastAnsweredAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setUser(User user) { this.user = user; }
    public void setQuestionType(PracticeQuestionType questionType) { this.questionType = questionType; }
    public void setTotalAttempted(Integer totalAttempted) { this.totalAttempted = totalAttempted; }
    public void setTotalCorrect(Integer totalCorrect) { this.totalCorrect = totalCorrect; }
    public void setTotalTimeTakenSeconds(Long totalTimeTakenSeconds) { this.totalTimeTakenSeconds = totalTimeTakenSeconds; }
    public void setAccuracyRate(BigDecimal accuracyRate) { this.accuracyRate = accuracyRate; }
    public void setLastAnsweredAt(LocalDateTime lastAnsweredAt) { this.lastAnsweredAt = lastAnsweredAt; }
}
