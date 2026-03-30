package com.skillforge.domain.practice.analysis;

import com.skillforge.common.enums.DifficultyLevel;
import com.skillforge.common.enums.PracticeQuestionType;

/**
 * Strategy interface for generating new practice questions when the database
 * does not contain sufficient questions for a given type/topic/difficulty.
 *
 * Implementations may call an external AI service, load from a question bank,
 * or any other mechanism. The default implementation logs a warning and is
 * intended to be replaced by a production-ready generator.
 *
 * This interface is intentionally isolated from the rest of the practice domain
 * so it can be swapped without touching any other module.
 */
public interface QuestionGenerationService {

    /**
     * Requests generation of new questions for the specified combination.
     *
     * @param type       the question type (PROGRAMMING_MCQ or APTITUDE_MCQ)
     * @param topic      the topic for which questions are needed (may be {@code null}
     *                   to indicate any topic)
     * @param difficulty the target difficulty level
     */
    void generateQuestions(PracticeQuestionType type, String topic, DifficultyLevel difficulty);
}
