package com.skillforge.domain.practice.dto;

public class PracticeRecommendationResponse {

    private String strategy;
    private String reason;
    private PracticeQuestionResponse question;

    private PracticeRecommendationResponse() {}

    public static PracticeRecommendationResponse of(String strategy, String reason, PracticeQuestionResponse question) {
        PracticeRecommendationResponse response = new PracticeRecommendationResponse();
        response.strategy = strategy;
        response.reason = reason;
        response.question = question;
        return response;
    }

    public String getStrategy() { return strategy; }
    public String getReason() { return reason; }
    public PracticeQuestionResponse getQuestion() { return question; }
}
