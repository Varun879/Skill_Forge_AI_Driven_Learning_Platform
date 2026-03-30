package com.skillforge.domain.execution.dto;

public class ExecutionEnqueueResponse {

    private Long executionId;
    private String status;

    public ExecutionEnqueueResponse(Long executionId, String status) {
        this.executionId = executionId;
        this.status = status;
    }

    public Long getExecutionId() { return executionId; }
    public String getStatus() { return status; }
}
