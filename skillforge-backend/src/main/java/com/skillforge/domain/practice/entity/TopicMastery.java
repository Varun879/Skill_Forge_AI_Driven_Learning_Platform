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
    name = "topic_mastery",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_topic_mastery_user_topic_type", columnNames = {"user_id", "topic", "question_type"})
    },
    indexes = {
        @Index(name = "idx_topic_mastery_user_mastery", columnList = "user_id,mastery_score"),
        @Index(name = "idx_topic_mastery_topic_type", columnList = "topic,question_type")
    }
)
public class TopicMastery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 120)
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 30)
    private PracticeQuestionType questionType;

    @Column(name = "attempted_count", nullable = false)
    private Integer attemptedCount = 0;

    @Column(name = "correct_count", nullable = false)
    private Integer correctCount = 0;

    @Column(name = "mastery_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal masteryScore = BigDecimal.ZERO;

    @Column(name = "avg_time_taken_seconds", nullable = false)
    private Integer avgTimeTakenSeconds = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_question_id")
    private PracticeQuestion recommendedQuestion;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public TopicMastery() {}

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getTopic() { return topic; }
    public PracticeQuestionType getQuestionType() { return questionType; }
    public Integer getAttemptedCount() { return attemptedCount; }
    public Integer getCorrectCount() { return correctCount; }
    public BigDecimal getMasteryScore() { return masteryScore; }
    public Integer getAvgTimeTakenSeconds() { return avgTimeTakenSeconds; }
    public PracticeQuestion getRecommendedQuestion() { return recommendedQuestion; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setUser(User user) { this.user = user; }
    public void setTopic(String topic) { this.topic = topic; }
    public void setQuestionType(PracticeQuestionType questionType) { this.questionType = questionType; }
    public void setAttemptedCount(Integer attemptedCount) { this.attemptedCount = attemptedCount; }
    public void setCorrectCount(Integer correctCount) { this.correctCount = correctCount; }
    public void setMasteryScore(BigDecimal masteryScore) { this.masteryScore = masteryScore; }
    public void setAvgTimeTakenSeconds(Integer avgTimeTakenSeconds) { this.avgTimeTakenSeconds = avgTimeTakenSeconds; }
    public void setRecommendedQuestion(PracticeQuestion recommendedQuestion) { this.recommendedQuestion = recommendedQuestion; }
}
