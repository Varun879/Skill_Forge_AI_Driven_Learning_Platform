package com.skillforge.domain.submission.dto;

public class SubmissionResultResponse {

    private final String status;
    private final String message;
    private final int totalTestCases;
    private final int passedTestCases;
    private final int score;

    public SubmissionResultResponse(String status, String message, int totalTestCases, int passedTestCases, int score) {
        this.status = status;
        this.message = message;
        this.totalTestCases = totalTestCases;
        this.passedTestCases = passedTestCases;
        this.score = score;
    }

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public int getTotalTestCases() { return totalTestCases; }
    public int getPassedTestCases() { return passedTestCases; }
    public int getScore() { return score; }
}
