package com.skillforge.domain.submission.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "submission_results")
public class SubmissionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private Submission submission;

    @Column(name = "passed_test_cases", nullable = false)
    private int passedTestCases;

    @Column(name = "total_test_cases", nullable = false)
    private int totalTestCases;

    @Column(nullable = false)
    private int score;

    @Column(columnDefinition = "TEXT")
    private String message;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public SubmissionResult() {}

    public Long getId() { return id; }
    public Submission getSubmission() { return submission; }
    public int getPassedTestCases() { return passedTestCases; }
    public int getTotalTestCases() { return totalTestCases; }
    public int getScore() { return score; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setSubmission(Submission submission) { this.submission = submission; }
    public void setPassedTestCases(int passedTestCases) { this.passedTestCases = passedTestCases; }
    public void setTotalTestCases(int totalTestCases) { this.totalTestCases = totalTestCases; }
    public void setScore(int score) { this.score = score; }
    public void setMessage(String message) { this.message = message; }
}
