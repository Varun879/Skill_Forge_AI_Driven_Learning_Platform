package com.skillforge.domain.practice.dto;

public class PracticeAnswerResponse {

    private Long questionId;
    private boolean correct;
    private Long selectedOptionId;
    private Long correctOptionId;
    private String explanation;
    private double updatedAccuracy;
    private String weakTopic;
    private PracticeRecommendationResponse nextRecommendation;

    public PracticeAnswerResponse(
            Long questionId,
            boolean correct,
            Long selectedOptionId,
            Long correctOptionId,
            String explanation,
            double updatedAccuracy,
            String weakTopic,
            PracticeRecommendationResponse nextRecommendation) {
        this.questionId = questionId;
        this.correct = correct;
        this.selectedOptionId = selectedOptionId;
        this.correctOptionId = correctOptionId;
        this.explanation = explanation;
        this.updatedAccuracy = updatedAccuracy;
        this.weakTopic = weakTopic;
        this.nextRecommendation = nextRecommendation;
    }

    public Long getQuestionId() { return questionId; }
    public boolean isCorrect() { return correct; }
    public Long getSelectedOptionId() { return selectedOptionId; }
    public Long getCorrectOptionId() { return correctOptionId; }
    public String getExplanation() { return explanation; }
    public double getUpdatedAccuracy() { return updatedAccuracy; }
    public String getWeakTopic() { return weakTopic; }
    public PracticeRecommendationResponse getNextRecommendation() { return nextRecommendation; }
}