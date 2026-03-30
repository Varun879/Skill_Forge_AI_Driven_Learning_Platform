package com.skillforge.domain.courseexam.dto;

import java.math.BigDecimal;

public class CourseExamAttemptResultResponse {

    private Long attemptId;
    private Long courseExamId;
    private BigDecimal score;
    private int totalQuestions;
    private int correctAnswers;
    private boolean passed;
    private String status;
    private boolean certificateIssued;

    public CourseExamAttemptResultResponse(Long attemptId,
                                           Long courseExamId,
                                           BigDecimal score,
                                           int totalQuestions,
                                           int correctAnswers,
                                           boolean passed,
                                           String status,
                                           boolean certificateIssued) {
        this.attemptId = attemptId;
        this.courseExamId = courseExamId;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.passed = passed;
        this.status = status;
        this.certificateIssued = certificateIssued;
    }

    public Long getAttemptId() { return attemptId; }
    public Long getCourseExamId() { return courseExamId; }
    public BigDecimal getScore() { return score; }
    public int getTotalQuestions() { return totalQuestions; }
    public int getCorrectAnswers() { return correctAnswers; }
    public boolean isPassed() { return passed; }
    public String getStatus() { return status; }
    public boolean isCertificateIssued() { return certificateIssued; }
}
