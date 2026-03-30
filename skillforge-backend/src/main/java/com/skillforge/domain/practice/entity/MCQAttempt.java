package com.skillforge.domain.practice.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.Immutable;

import com.skillforge.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Immutable
@Table(name = "user_answers")
public class MCQAttempt {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public MCQAttempt() {
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
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
}
