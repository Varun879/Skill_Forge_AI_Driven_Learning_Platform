package com.skillforge.domain.practice.recommendation.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillforge.common.enums.DifficultyLevel;
import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.domain.practice.dto.PracticeQuestionResponse;
import com.skillforge.domain.practice.entity.PracticeQuestion;
import com.skillforge.domain.practice.recommendation.dto.CategoryPerformanceDto;
import com.skillforge.domain.practice.recommendation.dto.NextQuestionResponse;
import com.skillforge.domain.practice.recommendation.dto.NextQuestionResponse.SelectionStrategy;
import com.skillforge.domain.practice.recommendation.repository.RecommendationQuestionRepository;
import com.skillforge.exception.ResourceNotFoundException;

/**
 * Core orchestrator for the AI-powered next-question flow.
 *
 * <h3>Full pipeline on each call</h3>
 * <ol>
 *   <li>Ensure every category has ≥ N questions (triggers AI generation if needed).</li>
 *   <li>Load the user's per-category performance.</li>
 *   <li>Apply the 3-tier priority: weak → medium → new categories.</li>
 *   <li>Adjust difficulty based on the user's last answer in the chosen category.</li>
 *   <li>Return one unattempted question or fall back to any active question.</li>
 * </ol>
 */
@Service
@Transactional(readOnly = true)
public class NextQuestionRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(NextQuestionRecommendationService.class);

    private final CategoryAvailabilityService availabilityService;
    private final UserPerformanceAnalyzer performanceAnalyzer;
    private final RecommendationQuestionRepository questionRepository;

    public NextQuestionRecommendationService(
            CategoryAvailabilityService availabilityService,
            UserPerformanceAnalyzer performanceAnalyzer,
            RecommendationQuestionRepository questionRepository) {
        this.availabilityService = availabilityService;
        this.performanceAnalyzer = performanceAnalyzer;
        this.questionRepository = questionRepository;
    }

    /**
     * Runs the full recommendation pipeline for the given user and question type.
     *
     * @param userId       learner's numeric user ID
     * @param questionType PROGRAMMING_MCQ or APTITUDE_MCQ
     * @return a {@link NextQuestionResponse} containing the selected question
     * @throws ResourceNotFoundException when no questions exist even after
     *                                   attempting generation
     */
    public NextQuestionResponse recommend(Long userId, PracticeQuestionType questionType) {
        // Step 1 — ensure categories are populated (may trigger AI generation)
        availabilityService.ensureCategoriesPopulated(questionType);

        // Step 2 — load performance data
        List<CategoryPerformanceDto> performances =
                performanceAnalyzer.getPerformance(userId, questionType);

        Set<String> attemptedCategories = new HashSet<>();
        for (CategoryPerformanceDto p : performances) {
            attemptedCategories.add(p.getCategory().toLowerCase());
        }

        // Step 3 — Priority 1: weak categories
        for (CategoryPerformanceDto perf : performances) {
            if (perf.isWeak()) {
                DifficultyLevel difficulty = adjustDifficulty(userId, questionType, perf.getCategory(), false);
                Optional<PracticeQuestion> q = pickUnattempted(userId, questionType, perf.getCategory(), difficulty);
                if (q.isPresent()) {
                    log.debug("Recommending WEAK category '{}' for user {}", perf.getCategory(), userId);
                    return buildResponse(q.get(), SelectionStrategy.WEAK_CATEGORY,
                            perf.getCategory(), "Reinforcing weak area: " + perf.getCategory());
                }
            }
        }

        // Step 4 — Priority 2: medium categories (60–80% accuracy)
        for (CategoryPerformanceDto perf : performances) {
            if (perf.isMedium()) {
                DifficultyLevel difficulty = adjustDifficulty(userId, questionType, perf.getCategory(), true);
                Optional<PracticeQuestion> q = pickUnattempted(userId, questionType, perf.getCategory(), difficulty);
                if (q.isPresent()) {
                    log.debug("Recommending MEDIUM category '{}' for user {}", perf.getCategory(), userId);
                    return buildResponse(q.get(), SelectionStrategy.MEDIUM_CATEGORY,
                            perf.getCategory(), "Improving medium area: " + perf.getCategory());
                }
            }
        }

        // Step 5 — Priority 3: new (never-attempted) categories
        List<String> allCategories = performanceAnalyzer.allCategoriesForType(questionType);
        for (String category : allCategories) {
            if (!attemptedCategories.contains(category.toLowerCase())) {
                Optional<PracticeQuestion> q = pickUnattempted(userId, questionType, category, DifficultyLevel.BEGINNER);
                if (q.isPresent()) {
                    log.debug("Recommending NEW category '{}' for user {}", category, userId);
                    return buildResponse(q.get(), SelectionStrategy.NEW_CATEGORY,
                            category, "Exploring new topic: " + category);
                }
            }
        }

        // Step 6 — Fallback: any active question of the type
        List<PracticeQuestion> fallback = questionRepository.findAnyUnattempted(
                userId, questionType, PageRequest.of(0, 1));
        if (fallback.isEmpty()) {
            fallback = questionRepository.findAnyActive(questionType, PageRequest.of(0, 1));
        }
        if (!fallback.isEmpty()) {
            PracticeQuestion q = fallback.get(0);
            log.debug("Recommending FALLBACK question for user {} type {}", userId, questionType);
            return buildResponse(q, SelectionStrategy.FALLBACK,
                    q.getTopic(), "General practice question");
        }

        throw new ResourceNotFoundException("No questions available for type: " + questionType);
    }

    public NextQuestionResponse recommendInCategory(Long userId, PracticeQuestionType questionType, String category) {
        String resolvedCategory = category == null ? null : category.trim();
        if (resolvedCategory == null || resolvedCategory.isBlank()) {
            return recommend(userId, questionType);
        }

        DifficultyLevel preferredDifficulty = adjustDifficulty(userId, questionType, resolvedCategory, true);

        Optional<PracticeQuestion> selected = pickUnattempted(userId, questionType, resolvedCategory, preferredDifficulty);
        if (selected.isEmpty()) {
            List<PracticeQuestion> sameCategoryUnattempted = questionRepository.findAnyUnattemptedByCategory(
                    userId,
                    questionType,
                    resolvedCategory,
                    PageRequest.of(0, 1));
            if (!sameCategoryUnattempted.isEmpty()) {
                selected = Optional.of(sameCategoryUnattempted.get(0));
            }
        }

        if (selected.isEmpty()) {
            List<PracticeQuestion> sameCategoryActive = questionRepository.findAnyActiveByCategory(
                    questionType,
                    resolvedCategory,
                    PageRequest.of(0, 1));
            if (!sameCategoryActive.isEmpty()) {
                selected = Optional.of(sameCategoryActive.get(0));
            }
        }

        if (selected.isPresent()) {
            return buildResponse(
                    selected.get(),
                    SelectionStrategy.FALLBACK,
                    resolvedCategory,
                    "Adaptive next question from category: " + resolvedCategory);
        }

        throw new ResourceNotFoundException("No questions available for category '" + resolvedCategory + "' and type: " + questionType);
    }

    // ------------------------------------------------------------------
    // private helpers
    // ------------------------------------------------------------------

    /**
     * Determines the appropriate difficulty based on the user's last answer
     * in the category.
     *
     * @param performingWell {@code true} when caller knows recent accuracy is
     *                       medium-range (use INTERMEDIATE as default)
     */
    private DifficultyLevel adjustDifficulty(
            Long userId, PracticeQuestionType type, String category, boolean performingWell) {

        List<PracticeQuestion> recent = questionRepository.findLastAttemptedQuestionInCategory(
                userId, type, category, PageRequest.of(0, 1));

        if (recent.isEmpty()) {
            return performingWell ? DifficultyLevel.INTERMEDIATE : DifficultyLevel.BEGINNER;
        }

        DifficultyLevel last = recent.get(0).getDifficultyLevel();
        List<Boolean> recentResults = questionRepository.findRecentIsCorrectByCategoryAndType(
                userId, type, category, PageRequest.of(0, 1));

        boolean lastCorrect = !recentResults.isEmpty() && Boolean.TRUE.equals(recentResults.get(0));

        if (lastCorrect) {
            // Increase difficulty: BEGINNER→INTERMEDIATE→ADVANCED
            return switch (last) {
                case BEGINNER     -> DifficultyLevel.INTERMEDIATE;
                case INTERMEDIATE -> DifficultyLevel.ADVANCED;
                case ADVANCED     -> DifficultyLevel.ADVANCED;
            };
        } else {
            // Decrease difficulty: ADVANCED→INTERMEDIATE→BEGINNER
            return switch (last) {
                case ADVANCED     -> DifficultyLevel.INTERMEDIATE;
                case INTERMEDIATE -> DifficultyLevel.BEGINNER;
                case BEGINNER     -> DifficultyLevel.BEGINNER;
            };
        }
    }

    private Optional<PracticeQuestion> pickUnattempted(
            Long userId, PracticeQuestionType type, String category, DifficultyLevel difficulty) {

        List<PracticeQuestion> candidates = questionRepository.findUnattemptedByCategoryAndDifficulty(
                userId, type, category, difficulty, PageRequest.of(0, 1));
        return candidates.isEmpty() ? Optional.empty() : Optional.of(candidates.get(0));
    }

    private NextQuestionResponse buildResponse(
            PracticeQuestion question,
            SelectionStrategy strategy,
            String category,
            String reason) {

        // PracticeQuestionResponse.from() loads options lazily — safe inside @Transactional
        PracticeQuestionResponse qResponse = PracticeQuestionResponse.from(question);
        return new NextQuestionResponse(strategy, category, reason, qResponse);
    }
}
