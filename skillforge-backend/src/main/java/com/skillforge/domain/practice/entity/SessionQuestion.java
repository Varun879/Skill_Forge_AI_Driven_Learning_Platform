package com.skillforge.domain.practice.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
    name = "session_questions",
    indexes = {
        @Index(name = "idx_session_questions_session_created", columnList = "session_id,created_at"),
        @Index(name = "idx_session_questions_question_id", columnList = "question_id")
    }
)
public class SessionQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private PracticeSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private PracticeQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private MCQOption selectedOption;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    @Column(name = "time_taken_seconds", nullable = false)
    private Integer timeTakenSeconds;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public SessionQuestion() {
    }

    public Long getId() {
        return id;
    }

    public PracticeSession getSession() {
        return session;
    }

    public PracticeQuestion getQuestion() {
        return question;
    }

    public MCQOption getSelectedOption() {
        return selectedOption;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public Integer getTimeTakenSeconds() {
        return timeTakenSeconds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setSession(PracticeSession session) {
        this.session = session;
    }

    public void setQuestion(PracticeQuestion question) {
        this.question = question;
    }

    public void setSelectedOption(MCQOption selectedOption) {
        this.selectedOption = selectedOption;
    }

    public void setIsCorrect(Boolean correct) {
        isCorrect = correct;
    }

    public void setTimeTakenSeconds(Integer timeTakenSeconds) {
        this.timeTakenSeconds = timeTakenSeconds;
    }
}
