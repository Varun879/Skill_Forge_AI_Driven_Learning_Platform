package com.skillforge.domain.practice.analysis;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillforge.common.enums.DifficultyLevel;
import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.domain.practice.analysis.repository.PracticeAnalysisRepository;
import com.skillforge.domain.practice.entity.PracticeQuestion;
import com.skillforge.domain.practice.repository.PracticeQuestionRepository;

/**
 * Internal service for the Practice analysis module responsible for:
 *
 * <ol>
 *   <li><strong>Availability check</strong> — verifies that questions exist in the
 *       database for the requested {@link PracticeQuestionType}.</li>
 *   <li><strong>On-demand generation</strong> — if no questions are found, delegates
 *       to {@link QuestionGenerationService} so that the system can be populated
 *       before serving a question.</li>
 *   <li><strong>Question fetch</strong> — retrieves filtered or unattempted questions
 *       by type, topic, and difficulty, used by {@link NextQuestionSelector}.</li>
 * </ol>
 *
 * This service is intentionally decoupled from the existing {@code PracticeService}
 * and does not modify any existing tables or APIs.
 */
@Service
@Transactional(readOnly = true)
public class PracticeQuestionService {

    private static final Logger log = LoggerFactory.getLogger(PracticeQuestionService.class);

    private final PracticeQuestionRepository questionRepository;
    private final PracticeAnalysisRepository analysisRepository;
    private final QuestionGenerationService generationService;

    public PracticeQuestionService(
            PracticeQuestionRepository questionRepository,
            PracticeAnalysisRepository analysisRepository,
            QuestionGenerationService generationService) {
        this.questionRepository = questionRepository;
        this.analysisRepository = analysisRepository;
        this.generationService = generationService;
    }

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    /**
     * Returns {@code true} when at least one active question exists in the
     * database for the requested type.
     *
     * @param type PROGRAMMING_MCQ or APTITUDE_MCQ
     */
    public boolean hasQuestionsForType(PracticeQuestionType type) {
        return analysisRepository.countActiveQuestionsByType(type) > 0;
    }

    /**
     * Fetches active questions filtered by type, difficulty, and topic
     * (any parameter may be {@code null} to widen the match).
     *
     * <p>If no questions exist for the given type at the time of the call,
     * the {@link QuestionGenerationService} is invoked before the database
     * query is executed.</p>
     *
     * @param type       question type (non-null)
     * @param difficulty filter by difficulty (nullable)
     * @param topic      filter by topic, case-insensitive (nullable)
     * @return possibly-empty list of matching active questions
     */
    public List<PracticeQuestion> fetchQuestionsForTopicAndDifficulty(
            PracticeQuestionType type,
            DifficultyLevel difficulty,
            String topic) {

        ensureQuestionsExist(type, topic, difficulty);
        return questionRepository.findFiltered(type, difficulty, topic);
    }

    /**
     * Returns a single unattempted question for the user that matches the
     * given type, topic, and difficulty level, or {@link Optional#empty()}
     * if no suitable question is available.
     *
     * <p>If no questions exist for the type, generation is requested first.</p>
     *
     * @param userId    the learner's numeric user ID
     * @param type      question type
     * @param topic     the topic to restrict to
     * @param difficulty the desired difficulty level
     */
    public Optional<PracticeQuestion> fetchUnattemptedForTopicDifficulty(
            Long userId,
            PracticeQuestionType type,
            String topic,
            DifficultyLevel difficulty) {

        ensureQuestionsExist(type, topic, difficulty);

        List<PracticeQuestion> candidates = analysisRepository
                .findUnattemptedQuestionsForUserTopicDifficulty(
                        userId, type, topic, difficulty, PageRequest.of(0, 1));

        return candidates.isEmpty() ? Optional.empty() : Optional.of(candidates.get(0));
    }

    // ------------------------------------------------------------------
    // private helpers
    // ------------------------------------------------------------------

    /**
     * Checks whether questions of the requested type exist; if not, triggers
     * the generation service so subsequent queries may find newly created data.
     */
    private void ensureQuestionsExist(
            PracticeQuestionType type, String topic, DifficultyLevel difficulty) {

        if (!hasQuestionsForType(type)) {
            log.info("No active questions found for type={}; requesting generation "
                    + "[topic={}, difficulty={}]", type, topic, difficulty);
            generationService.generateQuestions(type, topic, difficulty);
        }
    }
}
