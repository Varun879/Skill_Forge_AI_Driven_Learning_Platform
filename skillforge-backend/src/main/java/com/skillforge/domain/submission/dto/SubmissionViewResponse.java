package com.skillforge.domain.submission.dto;

import java.time.LocalDateTime;

import com.skillforge.domain.submission.entity.ReviewFeedback;
import com.skillforge.domain.submission.entity.Submission;
import com.skillforge.domain.submission.entity.SubmissionResult;

public class SubmissionViewResponse {

    private final Long id;
    private final Long problemId;
    private final String problemTitle;
    private final Long learnerId;
    private final String learnerName;
    private final String language;
    private final String sourceCode;
    private final String status;
    private final Integer score;
    private final Integer totalTestCases;
    private final Integer passedTestCases;
    private final String feedback;
    private final LocalDateTime reviewedAt;
    private final LocalDateTime createdAt;

    public SubmissionViewResponse(Long id,
                                  Long problemId,
                                  String problemTitle,
                                  Long learnerId,
                                  String learnerName,
                                  String language,
                                  String sourceCode,
                                  String status,
                                  Integer score,
                                  Integer totalTestCases,
                                  Integer passedTestCases,
                                  String feedback,
                                  LocalDateTime reviewedAt,
                                  LocalDateTime createdAt) {
        this.id = id;
        this.problemId = problemId;
        this.problemTitle = problemTitle;
        this.learnerId = learnerId;
        this.learnerName = learnerName;
        this.language = language;
        this.sourceCode = sourceCode;
        this.status = status;
        this.score = score;
        this.totalTestCases = totalTestCases;
        this.passedTestCases = passedTestCases;
        this.feedback = feedback;
        this.reviewedAt = reviewedAt;
        this.createdAt = createdAt;
    }

    public static SubmissionViewResponse from(Submission submission) {
        SubmissionResult result = submission.getResult();
        ReviewFeedback reviewFeedback = submission.getReviewFeedback();

        String firstName = submission.getLearner().getFirstName();
        String lastName = submission.getLearner().getLastName();
        String fullName = ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
        String learnerName = fullName.isEmpty() ? submission.getLearner().getUsername() : fullName;

        return new SubmissionViewResponse(
                submission.getId(),
                submission.getProblem().getId(),
                submission.getProblem().getTitle(),
                submission.getLearner().getId(),
                learnerName,
                submission.getLanguage(),
                submission.getSourceCode(),
                submission.getStatus(),
                result == null ? null : result.getScore(),
                result == null ? null : result.getTotalTestCases(),
                result == null ? null : result.getPassedTestCases(),
                reviewFeedback == null ? null : reviewFeedback.getFeedback(),
                reviewFeedback == null ? null : reviewFeedback.getReviewedAt(),
                submission.getCreatedAt());
    }

    public Long getId() { return id; }
    public Long getProblemId() { return problemId; }
    public String getProblemTitle() { return problemTitle; }
    public Long getLearnerId() { return learnerId; }
    public String getLearnerName() { return learnerName; }
    public String getLanguage() { return language; }
    public String getSourceCode() { return sourceCode; }
    public String getStatus() { return status; }
    public Integer getScore() { return score; }
    public Integer getTotalTestCases() { return totalTestCases; }
    public Integer getPassedTestCases() { return passedTestCases; }
    public String getFeedback() { return feedback; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
