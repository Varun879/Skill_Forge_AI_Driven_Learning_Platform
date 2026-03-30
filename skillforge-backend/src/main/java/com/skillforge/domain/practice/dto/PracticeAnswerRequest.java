package com.skillforge.domain.practice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class PracticeAnswerRequest {

    @NotNull(message = "questionId is required")
    private Long questionId;

    private Long selectedOptionId;

    private String codingAnswer;

    @NotNull(message = "timeTakenSeconds is required")
    @Min(value = 1, message = "timeTakenSeconds must be at least 1")
    private Integer timeTakenSeconds;

    public PracticeAnswerRequest() {}

    public Long getQuestionId() { return questionId; }
    public Long getSelectedOptionId() { return selectedOptionId; }
    public String getCodingAnswer() { return codingAnswer; }
    public Integer getTimeTakenSeconds() { return timeTakenSeconds; }

    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public void setSelectedOptionId(Long selectedOptionId) { this.selectedOptionId = selectedOptionId; }
    public void setCodingAnswer(String codingAnswer) { this.codingAnswer = codingAnswer; }
    public void setTimeTakenSeconds(Integer timeTakenSeconds) { this.timeTakenSeconds = timeTakenSeconds; }
}