package com.skillforge.domain.doubt.entity;

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
    name = "doubt_answers",
    indexes = {
        @Index(name = "idx_doubt_answers_doubt_id", columnList = "doubt_id"),
        @Index(name = "idx_doubt_answers_tutor_id", columnList = "tutor_id")
    }
)
public class DoubtAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doubt_id", nullable = false)
    private Doubt doubt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tutor_id", nullable = false)
    private User tutor;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public Doubt getDoubt() { return doubt; }
    public User getTutor() { return tutor; }
    public String getAnswer() { return answer; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setDoubt(Doubt doubt) { this.doubt = doubt; }
    public void setTutor(User tutor) { this.tutor = tutor; }
    public void setAnswer(String answer) { this.answer = answer; }
}