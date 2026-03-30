package com.skillforge.domain.practice.recommendation.service;

import com.skillforge.common.enums.PracticeQuestionType;

/**
 * Contract for generating MCQ questions via an external AI model.
 * Implementations persist the generated questions into the
 * {@code practice_questions}, {@code programming_mcqs}/{@code aptitude_mcqs},
 * and {@code mcq_options} tables.
 */
public interface AIQuestionGeneratorService {

    /**
     * Generates and persists questions for the given category and question type.
     *
     * @param category     the topic/category name (e.g. "Arrays", "Percentages")
     * @param questionType PROGRAMMING_MCQ or APTITUDE_MCQ
     * @param count        number of questions to attempt to generate
     */
    void generateAndSave(String category, PracticeQuestionType questionType, int count);
}
