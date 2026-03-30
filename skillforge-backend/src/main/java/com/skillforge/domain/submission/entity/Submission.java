package com.skillforge.domain.submission.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.skillforge.domain.problem.entity.Problem;
import com.skillforge.domain.user.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "submissions",
    indexes = {
        @Index(name = "idx_submissions_learner_id", columnList = "learner_id"),
        @Index(name = "idx_submissions_problem_id", columnList = "problem_id"),
        @Index(name = "idx_submissions_created_at", columnList = "created_at")
    }
)
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learner_id", nullable = false)
    private User learner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false, length = 30)
    private String language;

    @Column(name = "source_code", nullable = false, columnDefinition = "TEXT")
    private String sourceCode;

    @Column(nullable = false, length = 20)
    private String status;

    @OneToOne(mappedBy = "submission", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private SubmissionResult result;

    @OneToOne(mappedBy = "submission", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private ReviewFeedback reviewFeedback;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Submission() {}

    public Long getId() { return id; }
    public User getLearner() { return learner; }
    public Problem getProblem() { return problem; }
    public String getLanguage() { return language; }
    public String getSourceCode() { return sourceCode; }
    public String getStatus() { return status; }
    public SubmissionResult getResult() { return result; }
    public ReviewFeedback getReviewFeedback() { return reviewFeedback; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setLearner(User learner) { this.learner = learner; }
    public void setProblem(Problem problem) { this.problem = problem; }
    public void setLanguage(String language) { this.language = language; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
    public void setStatus(String status) { this.status = status; }
    public void setResult(SubmissionResult result) { this.result = result; }
    public void setReviewFeedback(ReviewFeedback reviewFeedback) { this.reviewFeedback = reviewFeedback; }
}
