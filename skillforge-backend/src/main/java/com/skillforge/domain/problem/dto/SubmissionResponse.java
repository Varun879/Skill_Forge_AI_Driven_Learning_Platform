package com.skillforge.domain.problem.dto;

public class SubmissionResponse {

    private String status;
    private String message;
    private int totalTestCases;

    public SubmissionResponse(String status, String message, int totalTestCases) {
        this.status = status;
        this.message = message;
        this.totalTestCases = totalTestCases;
    }

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public int getTotalTestCases() { return totalTestCases; }
}
