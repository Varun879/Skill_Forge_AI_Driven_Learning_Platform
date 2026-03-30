package com.skillforge.domain.practice.dto;

import java.time.LocalDateTime;

import com.skillforge.domain.practice.entity.MCQAttempt;

public class ProgrammingMCQAttemptResponse {

    private Long attemptId;
    private Long questionId;
    private Boolean isCorrect;
    private Integer timeTakenSeconds;
    private LocalDateTime attemptedAt;

    private ProgrammingMCQAttemptResponse() {
    }

    public static ProgrammingMCQAttemptResponse from(MCQAttempt attempt) {
        ProgrammingMCQAttemptResponse response = new ProgrammingMCQAttemptResponse();
        response.attemptId = attempt.getId();
        response.questionId = attempt.getQuestion().getId();
        response.isCorrect = attempt.getIsCorrect();
        response.timeTakenSeconds = attempt.getTimeTakenSeconds();
        response.attemptedAt = attempt.getCreatedAt();
        return response;
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public Integer getTimeTakenSeconds() {
        return timeTakenSeconds;
    }

    public LocalDateTime getAttemptedAt() {
        return attemptedAt;
    }
}
