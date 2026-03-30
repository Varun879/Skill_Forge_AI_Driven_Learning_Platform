package com.skillforge.domain.practice.entity;

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

@Entity
@Table(
    name = "user_answers",
    indexes = {
        @Index(name = "idx_user_answers_user_created_at", columnList = "user_id,created_at"),
        @Index(name = "idx_user_answers_session_id", columnList = "session_id"),
        @Index(name = "idx_user_answers_question_id", columnList = "question_id"),
        @Index(name = "idx_user_answers_user_question_created_at", columnList = "user_id,question_id,created_at"),
        @Index(name = "idx_user_answers_user_correct_created_at", columnList = "user_id,is_correct,created_at")
    }
)
public class UserAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private PracticeSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private PracticeQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private MCQOption selectedOption;

    @Column(name = "coding_answer", columnDefinition = "TEXT")
    private String codingAnswer;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    @Column(name = "time_taken_seconds", nullable = false)
    private Integer timeTakenSeconds;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UserAnswer() {}

    public Long getId() { return id; }
    public User getUser() { return user; }
    public PracticeSession getSession() { return session; }
    public PracticeQuestion getQuestion() { return question; }
    public MCQOption getSelectedOption() { return selectedOption; }
    public String getCodingAnswer() { return codingAnswer; }
    public Boolean getIsCorrect() { return isCorrect; }
    public Integer getTimeTakenSeconds() { return timeTakenSeconds; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setUser(User user) { this.user = user; }
    public void setSession(PracticeSession session) { this.session = session; }
    public void setQuestion(PracticeQuestion question) { this.question = question; }
    public void setSelectedOption(MCQOption selectedOption) { this.selectedOption = selectedOption; }
    public void setCodingAnswer(String codingAnswer) { this.codingAnswer = codingAnswer; }
    public void setIsCorrect(Boolean correct) { isCorrect = correct; }
    public void setTimeTakenSeconds(Integer timeTakenSeconds) { this.timeTakenSeconds = timeTakenSeconds; }
}
