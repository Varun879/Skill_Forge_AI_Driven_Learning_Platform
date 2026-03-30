package com.skillforge.domain.execution.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.skillforge.domain.execution.entity.Execution;

public class ExecutionResultResponse {

    private Long executionId;
    private Long problemId;
    private String mode;
    private String language;
    private String status;
    private String stdout;
    private String stderr;
    private Long executionTimeMs;
    private Long memoryUsageKb;
    private boolean cacheHit;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private List<ExecutionTestCaseResultResponse> testCaseResults;

    public static ExecutionResultResponse from(Execution execution) {
        ExecutionResultResponse response = new ExecutionResultResponse();
        response.executionId = execution.getId();
        response.problemId = execution.getProblem().getId();
        response.mode = execution.getMode().name();
        response.language = execution.getLanguage();
        response.status = execution.getStatus().name();
        response.stdout = execution.getStdoutData();
        response.stderr = execution.getStderrData();
        response.executionTimeMs = execution.getExecutionTimeMs();
        response.memoryUsageKb = execution.getMemoryUsageKb();
        response.cacheHit = execution.isCacheHit();
        response.createdAt = execution.getCreatedAt();
        response.completedAt = execution.getCompletedAt();
        response.testCaseResults = execution.getTestResults().stream()
                .map(ExecutionTestCaseResultResponse::from)
                .toList();
        return response;
    }

    public Long getExecutionId() { return executionId; }
    public Long getProblemId() { return problemId; }
    public String getMode() { return mode; }
    public String getLanguage() { return language; }
    public String getStatus() { return status; }
    public String getStdout() { return stdout; }
    public String getStderr() { return stderr; }
    public Long getExecutionTimeMs() { return executionTimeMs; }
    public Long getMemoryUsageKb() { return memoryUsageKb; }
    public boolean isCacheHit() { return cacheHit; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public List<ExecutionTestCaseResultResponse> getTestCaseResults() { return testCaseResults; }
}
