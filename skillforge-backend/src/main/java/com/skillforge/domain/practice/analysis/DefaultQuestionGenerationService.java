package com.skillforge.domain.practice.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.skillforge.common.enums.DifficultyLevel;
import com.skillforge.common.enums.PracticeQuestionType;

/**
 * Default (stub) implementation of {@link QuestionGenerationService}.
 *
 * This implementation does NOT generate questions — it emits a warning log so
 * that operators know generation was requested but no external generator is
 * wired up. Replace or extend this bean with an AI-backed or content-managed
 * implementation when question generation becomes available.
 *
 * Marking this as {@code @Service} with {@code @Primary} is unnecessary unless
 * a second implementation coexists in the context.
 */
@Service
public class DefaultQuestionGenerationService implements QuestionGenerationService {

    private static final Logger log = LoggerFactory.getLogger(DefaultQuestionGenerationService.class);

    @Override
    public void generateQuestions(PracticeQuestionType type, String topic, DifficultyLevel difficulty) {
        log.warn(
                "Question generation requested but no external generator is configured. "
                + "Practice system will fall back to existing seed data. "
                + "[type={}, topic={}, difficulty={}]",
                type, topic, difficulty);
    }
}
