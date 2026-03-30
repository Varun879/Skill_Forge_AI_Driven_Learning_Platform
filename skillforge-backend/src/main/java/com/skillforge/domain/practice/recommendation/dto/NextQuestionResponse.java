package com.skillforge.domain.practice.recommendation.dto;

import com.skillforge.domain.practice.dto.PracticeQuestionResponse;

/**
 * Response payload for {@code GET /api/practice/next-question}.
 */
public class NextQuestionResponse {

    /** How the question was selected. */
    public enum SelectionStrategy {
        WEAK_CATEGORY,
        MEDIUM_CATEGORY,
        NEW_CATEGORY,
        FALLBACK
    }

    private final SelectionStrategy strategy;
    private final String category;
    private final String reason;
    private final PracticeQuestionResponse question;

    public NextQuestionResponse(
            SelectionStrategy strategy,
            String category,
            String reason,
            PracticeQuestionResponse question) {
        this.strategy = strategy;
        this.category = category;
        this.reason = reason;
        this.question = question;
    }

    public SelectionStrategy getStrategy() { return strategy; }
    public String getCategory() { return category; }
    public String getReason() { return reason; }
    public PracticeQuestionResponse getQuestion() { return question; }
}
