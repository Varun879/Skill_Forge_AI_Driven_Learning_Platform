package com.skillforge.domain.practice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.skillforge.common.enums.PracticeSessionType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "practice_sessions",
    indexes = {
        @Index(name = "idx_practice_sessions_user_started_at", columnList = "user_id,started_at"),
        @Index(name = "idx_practice_sessions_user_type_started_at", columnList = "user_id,session_type,started_at")
    }
)
public class PracticeSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false, length = 30)
    private PracticeSessionType sessionType;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "total_time_taken_seconds")
    private Integer totalTimeTakenSeconds;

    @Column(name = "accuracy_rate", precision = 5, scale = 2)
    private BigDecimal accuracyRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_question_id")
    private PracticeQuestion recommendedQuestion;

    @OneToMany(mappedBy = "session", fetch = FetchType.LAZY)
    private List<UserAnswer> answers = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PracticeSession() {}

    public Long getId() { return id; }
    public User getUser() { return user; }
    public PracticeSessionType getSessionType() { return sessionType; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public Integer getTotalTimeTakenSeconds() { return totalTimeTakenSeconds; }
    public BigDecimal getAccuracyRate() { return accuracyRate; }
    public PracticeQuestion getRecommendedQuestion() { return recommendedQuestion; }
    public List<UserAnswer> getAnswers() { return answers; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setUser(User user) { this.user = user; }
    public void setSessionType(PracticeSessionType sessionType) { this.sessionType = sessionType; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
    public void setTotalTimeTakenSeconds(Integer totalTimeTakenSeconds) { this.totalTimeTakenSeconds = totalTimeTakenSeconds; }
    public void setAccuracyRate(BigDecimal accuracyRate) { this.accuracyRate = accuracyRate; }
    public void setRecommendedQuestion(PracticeQuestion recommendedQuestion) { this.recommendedQuestion = recommendedQuestion; }
}
