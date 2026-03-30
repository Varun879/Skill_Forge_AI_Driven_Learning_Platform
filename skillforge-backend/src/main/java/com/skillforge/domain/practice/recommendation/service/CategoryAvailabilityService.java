package com.skillforge.domain.practice.recommendation.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.skillforge.common.enums.AptitudeMCQCategory;
import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.common.enums.ProgrammingMCQCategory;
import com.skillforge.domain.practice.entity.PracticeQuestion;
import com.skillforge.domain.practice.recommendation.repository.RecommendationQuestionRepository;
import com.skillforge.domain.practice.repository.PracticeQuestionRepository;

/**
 * Ensures every defined category has at least the configured minimum number
 * of active questions, triggering AI generation for categories that are low.
 *
 * <p>Called once per {@code /practice/next-question} request before question
 * selection. The generation call itself is fire-and-check: if generation
 * succeeds the new questions are immediately available to the selector.</p>
 */
@Service
public class CategoryAvailabilityService {

    private static final Logger log = LoggerFactory.getLogger(CategoryAvailabilityService.class);

    @Value("${practice.ai.min-questions-per-category:10}")
    private int minQuestionsPerCategory;

    @Value("${practice.ai.require-real-questions:true}")
    private boolean requireRealQuestions;

    @Value("${practice.ai.max-categories-to-topup-per-request:2}")
    private int maxCategoriesToTopupPerRequest;

    private final RecommendationQuestionRepository questionRepository;
    private final PracticeQuestionRepository practiceQuestionRepository;
    private final AIQuestionGeneratorService generatorService;
    private final MCQGenerationService mcqGenerationService;

    public CategoryAvailabilityService(
            RecommendationQuestionRepository questionRepository,
            PracticeQuestionRepository practiceQuestionRepository,
            AIQuestionGeneratorService generatorService,
            MCQGenerationService mcqGenerationService) {
        this.questionRepository = questionRepository;
        this.practiceQuestionRepository = practiceQuestionRepository;
        this.generatorService = generatorService;
        this.mcqGenerationService = mcqGenerationService;
    }

    /**
     * Checks every category for the given question type. For each category
     * that has fewer than {@code practice.ai.min-questions-per-category}
     * active questions the AI generator is invoked to top it up.
     *
     * @param questionType PROGRAMMING_MCQ or APTITUDE_MCQ
     */
    public void ensureCategoriesPopulated(PracticeQuestionType questionType) {
        int resolvedLimit = Math.max(1, maxCategoriesToTopupPerRequest);
        int processed = 0;

        if (questionType == PracticeQuestionType.PROGRAMMING_MCQ) {
            for (ProgrammingMCQCategory cat : ProgrammingMCQCategory.values()) {
                if (checkAndGenerate(cat.getDisplayName(), questionType)) {
                    processed++;
                    if (processed >= resolvedLimit) {
                        break;
                    }
                }
            }
        } else if (questionType == PracticeQuestionType.APTITUDE_MCQ) {
            for (AptitudeMCQCategory cat : AptitudeMCQCategory.values()) {
                if (checkAndGenerate(cat.getDisplayName(), questionType)) {
                    processed++;
                    if (processed >= resolvedLimit) {
                        break;
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------

    private boolean checkAndGenerate(String category, PracticeQuestionType type) {
        long count = countRealQuestions(category, type);
        if (count < minQuestionsPerCategory) {
            int needed = (int) (minQuestionsPerCategory - count);
            log.info("Category '{}' has {} questions (min {}). Requesting AI generation of {} more.",
                    category, count, minQuestionsPerCategory, needed);
            try {
                generatorService.generateAndSave(category, type, needed);
            } catch (Exception ex) {
                log.warn("AI generation failed for category '{}' type {}: {}", category, type, ex.getMessage());
            }

            long postGenerationCount = countRealQuestions(category, type);
            if (postGenerationCount < minQuestionsPerCategory) {
                if (requireRealQuestions) {
                    log.warn("Category '{}' still below minimum after AI generation ({} < {}). Real-question mode is enabled; using local fallback to keep adaptive flow available.",
                        category, postGenerationCount, minQuestionsPerCategory);
                    mcqGenerationService.populateIfBelowMinimum(type, minQuestionsPerCategory);
                    return true;
                }

                log.warn("Category '{}' still below minimum after AI generation ({} < {}). Falling back to local generation.",
                        category, postGenerationCount, minQuestionsPerCategory);
                mcqGenerationService.populateIfBelowMinimum(type, minQuestionsPerCategory);
            }

            return true;
        }

        return false;
    }

    private long countRealQuestions(String category, PracticeQuestionType type) {
        List<PracticeQuestion> questions = practiceQuestionRepository.findFiltered(type, null, category);
        return questions.stream()
                .filter(question -> question.getTags() == null
                        || question.getTags().stream().noneMatch(tag -> "auto-generated".equalsIgnoreCase(tag)))
                .count();
    }
}
