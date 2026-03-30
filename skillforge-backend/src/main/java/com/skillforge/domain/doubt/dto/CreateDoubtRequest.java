package com.skillforge.domain.doubt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateDoubtRequest {

    @NotNull(message = "Problem id is required")
    private Long problemId;

    @NotBlank(message = "Question is required")
    @Size(max = 2000, message = "Question must not exceed 2000 characters")
    private String question;

    public Long getProblemId() { return problemId; }
    public String getQuestion() { return question; }

    public void setProblemId(Long problemId) { this.problemId = problemId; }
    public void setQuestion(String question) { this.question = question; }
}