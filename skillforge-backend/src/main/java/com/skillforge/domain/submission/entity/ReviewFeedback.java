package com.skillforge.domain.submission.entity;

import java.time.LocalDateTime;

import com.skillforge.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "review_feedbacks")
public class ReviewFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private Submission submission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tutor_id", nullable = false)
    private User tutor;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "reviewed_at", nullable = false)
    private LocalDateTime reviewedAt;

    public ReviewFeedback() {}

    public Long getId() { return id; }
    public Submission getSubmission() { return submission; }
    public User getTutor() { return tutor; }
    public String getFeedback() { return feedback; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }

    public void setSubmission(Submission submission) { this.submission = submission; }
    public void setTutor(User tutor) { this.tutor = tutor; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
