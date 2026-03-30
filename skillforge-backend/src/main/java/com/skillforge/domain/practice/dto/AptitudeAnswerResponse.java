package com.skillforge.domain.practice.dto;

import java.util.List;

public class AptitudeAnswerResponse {

    private Long questionId;
    private Boolean isCorrect;
    private Long selectedOptionId;
    private Long correctOptionId;
    private String explanation;
    private Integer timeTakenSeconds;
    private Double accuracyRate;
    private Double averageTimeTakenSeconds;
    private List<AptitudeAttemptResponse> attemptHistory;
    private PracticeRecommendationResponse nextQuestion;

    public AptitudeAnswerResponse(
            Long questionId,
            Boolean isCorrect,
            Long selectedOptionId,
            Long correctOptionId,
            String explanation,
            Integer timeTakenSeconds,
            Double accuracyRate,
            Double averageTimeTakenSeconds,
            List<AptitudeAttemptResponse> attemptHistory,
            PracticeRecommendationResponse nextQuestion) {
        this.questionId = questionId;
        this.isCorrect = isCorrect;
        this.selectedOptionId = selectedOptionId;
        this.correctOptionId = correctOptionId;
        this.explanation = explanation;
        this.timeTakenSeconds = timeTakenSeconds;
        this.accuracyRate = accuracyRate;
        this.averageTimeTakenSeconds = averageTimeTakenSeconds;
        this.attemptHistory = attemptHistory;
        this.nextQuestion = nextQuestion;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public Long getSelectedOptionId() {
        return selectedOptionId;
    }

    public Long getCorrectOptionId() {
        return correctOptionId;
    }

    public String getExplanation() {
        return explanation;
    }

    public Integer getTimeTakenSeconds() {
        return timeTakenSeconds;
    }

    public Double getAccuracyRate() {
        return accuracyRate;
    }

    public Double getAverageTimeTakenSeconds() {
        return averageTimeTakenSeconds;
    }

    public List<AptitudeAttemptResponse> getAttemptHistory() {
        return attemptHistory;
    }

    public PracticeRecommendationResponse getNextQuestion() {
        return nextQuestion;
    }
}
