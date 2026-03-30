package com.skillforge.domain.practice.entity;

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

/**
 * Tracks a learner's performance per category and question type.
 *
 * <p>One row exists per {@code (user, category, questionType)} triple.
 * Rows are upserted by {@link com.skillforge.domain.practice.recommendation.service.UserPerformanceAnalyzer}
 * when the {@code /practice/next-question} endpoint is called.</p>
 *
 * <p>This table is additive — it does NOT replace or alter {@code topic_mastery}.</p>
 */
@Entity
@Table(
    name = "user_topic_performance",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_user_topic_perf_user_category_type",
            columnNames = {"user_id", "category", "question_type"})
    },
    indexes = {
        @Index(name = "idx_utp_user_type", columnList = "user_id,question_type"),
        @Index(name = "idx_utp_category_type", columnList = "category,question_type")
    }
)
public class UserTopicPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The category name (matches {@code topic} in {@code practice_questions}). */
    @Column(nullable = false, length = 120)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 30)
    private PracticeQuestionType questionType;

    @Column(name = "number_of_attempts", nullable = false)
    private int numberOfAttempts = 0;

    @Column(name = "correct_attempts", nullable = false)
    private int correctAttempts = 0;

    /**
     * Accuracy ratio in [0.0, 1.0]. Stored for fast filtering.
     * Recomputed on each upsert as {@code correctAttempts / numberOfAttempts}.
     */
    @Column(name = "accuracy", nullable = false)
    private double accuracy = 0.0;

    /** Rolling average solve time in seconds. */
    @Column(name = "average_solve_time_seconds", nullable = false)
    private double averageSolveTimeSeconds = 0.0;

    /**
     * Count of consecutive wrong answers in this category.
     * Reset to 0 on a correct answer; capped tracking at 3.
     * A value of {@code >= 3} triggers the CONSECUTIVE_FAILURES weak signal.
     */
    @Column(name = "recent_wrong_streak", nullable = false)
    private int recentWrongStreak = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public UserTopicPerformance() {}

    // ---- getters ----
    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getCategory() { return category; }
    public PracticeQuestionType getQuestionType() { return questionType; }
    public int getNumberOfAttempts() { return numberOfAttempts; }
    public int getCorrectAttempts() { return correctAttempts; }
    public double getAccuracy() { return accuracy; }
    public double getAverageSolveTimeSeconds() { return averageSolveTimeSeconds; }
    public int getRecentWrongStreak() { return recentWrongStreak; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // ---- setters ----
    public void setUser(User user) { this.user = user; }
    public void setCategory(String category) { this.category = category; }
    public void setQuestionType(PracticeQuestionType questionType) { this.questionType = questionType; }
    public void setNumberOfAttempts(int numberOfAttempts) { this.numberOfAttempts = numberOfAttempts; }
    public void setCorrectAttempts(int correctAttempts) { this.correctAttempts = correctAttempts; }
    public void setAccuracy(double accuracy) { this.accuracy = accuracy; }
    public void setAverageSolveTimeSeconds(double averageSolveTimeSeconds) { this.averageSolveTimeSeconds = averageSolveTimeSeconds; }
    public void setRecentWrongStreak(int recentWrongStreak) { this.recentWrongStreak = recentWrongStreak; }
}
