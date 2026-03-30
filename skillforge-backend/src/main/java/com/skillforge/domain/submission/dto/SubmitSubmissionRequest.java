package com.skillforge.domain.submission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SubmitSubmissionRequest {

    @NotNull(message = "Problem id is required")
    private Long problemId;

    @NotBlank(message = "Language is required")
    private String language;

    @NotBlank(message = "Source code is required")
    private String sourceCode;

    public Long getProblemId() { return problemId; }
    public String getLanguage() { return language; }
    public String getSourceCode() { return sourceCode; }

    public void setProblemId(Long problemId) { this.problemId = problemId; }
    public void setLanguage(String language) { this.language = language; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
}
