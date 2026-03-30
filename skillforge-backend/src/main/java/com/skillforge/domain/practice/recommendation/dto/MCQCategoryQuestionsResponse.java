package com.skillforge.domain.practice.recommendation.dto;

import java.util.List;

import com.skillforge.domain.practice.dto.PracticeQuestionResponse;

public class MCQCategoryQuestionsResponse {

    private final String category;
    private final List<PracticeQuestionResponse> questions;

    public MCQCategoryQuestionsResponse(String category, List<PracticeQuestionResponse> questions) {
        this.category = category;
        this.questions = questions;
    }

    public String getCategory() {
        return category;
    }

    public List<PracticeQuestionResponse> getQuestions() {
        return questions;
    }
}
