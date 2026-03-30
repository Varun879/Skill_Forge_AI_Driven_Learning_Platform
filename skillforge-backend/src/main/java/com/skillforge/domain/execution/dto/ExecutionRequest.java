package com.skillforge.domain.execution.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ExecutionRequest {

    @NotNull(message = "Problem id is required")
    private Long problemId;

    @NotBlank(message = "Language is required")
    private String language;

    @NotBlank(message = "Source code is required")
    private String sourceCode;

    private String stdin;

    public Long getProblemId() { return problemId; }
    public String getLanguage() { return language; }
    public String getSourceCode() { return sourceCode; }
    public String getStdin() { return stdin; }

    public void setProblemId(Long problemId) { this.problemId = problemId; }
    public void setLanguage(String language) { this.language = language; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
    public void setStdin(String stdin) { this.stdin = stdin; }
}
