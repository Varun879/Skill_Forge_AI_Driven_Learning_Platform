package com.skillforge.domain.practice.recommendation.dto;

import java.util.List;

import com.skillforge.domain.practice.dto.PracticeQuestionResponse;

public class NextQuestionSetResponse {

    private final String category;
    private final int requestedCount;
    private final int generatedCount;
    private final double lastTenAccuracy;
    private final List<PracticeQuestionResponse> questions;

    public NextQuestionSetResponse(
            String category,
            int requestedCount,
            int generatedCount,
            double lastTenAccuracy,
            List<PracticeQuestionResponse> questions) {
        this.category = category;
        this.requestedCount = requestedCount;
        this.generatedCount = generatedCount;
        this.lastTenAccuracy = lastTenAccuracy;
        this.questions = questions;
    }

    public String getCategory() { return category; }

    public int getRequestedCount() { return requestedCount; }

    public int getGeneratedCount() { return generatedCount; }

    public double getLastTenAccuracy() { return lastTenAccuracy; }

    public List<PracticeQuestionResponse> getQuestions() { return questions; }
}
