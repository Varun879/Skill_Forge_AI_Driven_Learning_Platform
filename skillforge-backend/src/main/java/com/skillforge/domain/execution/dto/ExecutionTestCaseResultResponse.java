package com.skillforge.domain.execution.dto;

import com.skillforge.domain.execution.entity.ExecutionTestResult;

public class ExecutionTestCaseResultResponse {

    private Long testCaseId;
    private boolean sample;
    private boolean passed;
    private String status;
    private String expectedOutput;
    private String actualOutput;
    private String stdout;
    private String stderr;
    private Long executionTimeMs;
    private Long memoryUsageKb;

    public static ExecutionTestCaseResultResponse from(ExecutionTestResult result) {
        ExecutionTestCaseResultResponse response = new ExecutionTestCaseResultResponse();
        response.testCaseId = result.getTestCase() != null ? result.getTestCase().getId() : null;
        response.sample = result.isSample();
        response.passed = result.isPassed();
        response.status = result.getStatus();
        response.expectedOutput = result.getExpectedOutput();
        response.actualOutput = result.getActualOutput();
        response.stdout = result.getStdoutData();
        response.stderr = result.getStderrData();
        response.executionTimeMs = result.getExecutionTimeMs();
        response.memoryUsageKb = result.getMemoryUsageKb();
        return response;
    }

    public Long getTestCaseId() { return testCaseId; }
    public boolean isSample() { return sample; }
    public boolean isPassed() { return passed; }
    public String getStatus() { return status; }
    public String getExpectedOutput() { return expectedOutput; }
    public String getActualOutput() { return actualOutput; }
    public String getStdout() { return stdout; }
    public String getStderr() { return stderr; }
    public Long getExecutionTimeMs() { return executionTimeMs; }
    public Long getMemoryUsageKb() { return memoryUsageKb; }
}
