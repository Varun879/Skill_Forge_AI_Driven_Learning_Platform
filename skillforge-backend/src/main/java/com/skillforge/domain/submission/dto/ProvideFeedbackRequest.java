package com.skillforge.domain.submission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProvideFeedbackRequest {

    @NotNull(message = "Submission id is required")
    private Long submissionId;

    @NotBlank(message = "Feedback is required")
    private String feedback;

    public Long getSubmissionId() { return submissionId; }
    public String getFeedback() { return feedback; }

    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
