package com.skillforge.domain.practice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AptitudeAnswerRequest {

    @NotNull(message = "questionId is required")
    private Long questionId;

    @NotNull(message = "selectedOptionId is required")
    private Long selectedOptionId;

    @NotNull(message = "timeTakenSeconds is required")
    @Min(value = 1, message = "timeTakenSeconds must be at least 1")
    @JsonAlias("timeTaken")
    private Integer timeTakenSeconds;

    public AptitudeAnswerRequest() {
    }

    public Long getQuestionId() {
        return questionId;
    }

    public Long getSelectedOptionId() {
        return selectedOptionId;
    }

    public Integer getTimeTakenSeconds() {
        return timeTakenSeconds;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public void setSelectedOptionId(Long selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }

    public void setTimeTakenSeconds(Integer timeTakenSeconds) {
        this.timeTakenSeconds = timeTakenSeconds;
    }
}
