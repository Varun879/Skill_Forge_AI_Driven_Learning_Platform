package com.skillforge.domain.practice.dto;

import java.time.LocalDateTime;

import com.skillforge.common.enums.DifficultyLevel;
import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.domain.practice.entity.SessionQuestion;

public class SessionQuestionResponse {

    private Long id;
    private Long questionId;
    private PracticeQuestionType questionType;
    private String topic;
    private DifficultyLevel difficultyLevel;
    private Boolean isCorrect;
    private Integer timeTakenSeconds;
    private LocalDateTime attemptedAt;

    private SessionQuestionResponse() {
    }

    public static SessionQuestionResponse from(SessionQuestion sessionQuestion) {
        SessionQuestionResponse response = new SessionQuestionResponse();
        response.id = sessionQuestion.getId();
        response.questionId = sessionQuestion.getQuestion().getId();
        response.questionType = sessionQuestion.getQuestion().getQuestionType();
        response.topic = sessionQuestion.getQuestion().getTopic();
        response.difficultyLevel = sessionQuestion.getQuestion().getDifficultyLevel();
        response.isCorrect = sessionQuestion.getIsCorrect();
        response.timeTakenSeconds = sessionQuestion.getTimeTakenSeconds();
        response.attemptedAt = sessionQuestion.getCreatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public PracticeQuestionType getQuestionType() {
        return questionType;
    }

    public String getTopic() {
        return topic;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
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
