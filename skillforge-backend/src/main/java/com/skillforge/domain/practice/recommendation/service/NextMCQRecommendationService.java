package com.skillforge.domain.practice.recommendation.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillforge.common.enums.DifficultyLevel;
import com.skillforge.domain.practice.recommendation.dto.CategoryPerformanceDto;
import com.skillforge.domain.practice.recommendation.dto.MCQTypeParam;
import com.skillforge.domain.practice.recommendation.dto.NextQuestionResponse;
import com.skillforge.domain.practice.recommendation.dto.NextQuestionSetResponse;
import com.skillforge.domain.practice.recommendation.repository.RecommendationQuestionRepository;

/**
 * Contract-matching service for MCQ recommendation flow.
 *
 * Flow:
 * 1) Check/populate MCQ tables if empty
 * 2) Analyze performance + weak areas
 * 3) Return best next question
 */
@Service
public class NextMCQRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(NextMCQRecommendationService.class);

    private final MCQGenerationService mcqGenerationService;
    private final NextQuestionRecommendationService nextQuestionRecommendationService;
    private final UserPerformanceAnalyzer userPerformanceAnalyzer;
    private final RecommendationQuestionRepository recommendationQuestionRepository;
    private final AIQuestionGeneratorService aiQuestionGeneratorService;

    public NextMCQRecommendationService(
            MCQGenerationService mcqGenerationService,
            NextQuestionRecommendationService nextQuestionRecommendationService,
            UserPerformanceAnalyzer userPerformanceAnalyzer,
            RecommendationQuestionRepository recommendationQuestionRepository,
            AIQuestionGeneratorService aiQuestionGeneratorService) {
        this.mcqGenerationService = mcqGenerationService;
        this.nextQuestionRecommendationService = nextQuestionRecommendationService;
        this.userPerformanceAnalyzer = userPerformanceAnalyzer;
        this.recommendationQuestionRepository = recommendationQuestionRepository;
        this.aiQuestionGeneratorService = aiQuestionGeneratorService;
    }

    @Transactional(readOnly = true)
    public NextQuestionResponse getNext(Long userId, MCQTypeParam type) {
        return getNext(userId, type, null);
    }

    @Transactional(readOnly = true)
    public NextQuestionResponse getNext(Long userId, MCQTypeParam type, String category) {
        var questionType = type.toPracticeQuestionType();
        mcqGenerationService.populateIfBelowMinimum(questionType, 10);

        if (category != null && !category.isBlank()) {
            String resolvedCategory = category.trim();
            int attempts = userPerformanceAnalyzer.getPerformanceWithAllCategories(userId, questionType).stream()
                    .filter(item -> item.getCategory().equalsIgnoreCase(resolvedCategory))
                    .map(CategoryPerformanceDto::getNumberOfAttempts)
                    .findFirst()
                    .orElse(0);

            int completedBlocks = attempts / 10;
            int targetPool = Math.max(10, (completedBlocks + 1) * 10);
            mcqGenerationService.populateCategoryIfBelowMinimum(questionType, resolvedCategory, targetPool);

            return nextQuestionRecommendationService.recommendInCategory(userId, questionType, resolvedCategory);
        }

        return nextQuestionRecommendationService.recommend(userId, questionType);
    }

    @Transactional(readOnly = true)
    public NextQuestionSetResponse getNextSet(Long userId, MCQTypeParam type, String category, int size) {
        var questionType = type.toPracticeQuestionType();
        String resolvedCategory = category == null ? "" : category.trim();
        if (resolvedCategory.isBlank()) {
            throw new IllegalArgumentException("category is required for adaptive next set");
        }

        int requestedSize = Math.max(1, Math.min(size, 20));

        mcqGenerationService.populateIfBelowMinimum(questionType, 10);

        int attempts = userPerformanceAnalyzer.getPerformanceWithAllCategories(userId, questionType).stream()
                .filter(item -> item.getCategory().equalsIgnoreCase(resolvedCategory))
                .map(CategoryPerformanceDto::getNumberOfAttempts)
                .findFirst()
                .orElse(0);

        int completedBlocks = attempts / 10;
        int targetPool = Math.max(10, (completedBlocks + 2) * 10);

        long currentCategoryPool = recommendationQuestionRepository.countByCategoryAndType(resolvedCategory, questionType);
        int aiNeeded = (int) Math.max(0, targetPool - currentCategoryPool);
        if (aiNeeded > 0) {
            int batch = Math.min(20, Math.max(10, aiNeeded));
            try {
                log.info("Adaptive next-set: requesting AI generation for category='{}', type={}, count={} (currentPool={}, targetPool={})",
                        resolvedCategory, questionType, batch, currentCategoryPool, targetPool);
                aiQuestionGeneratorService.generateAndSave(resolvedCategory, questionType, batch);
            } catch (Exception ex) {
                log.warn("Adaptive next-set AI generation failed for category='{}', type={}: {}",
                        resolvedCategory, questionType, ex.getMessage());
            }
        }

        mcqGenerationService.populateCategoryIfBelowMinimum(questionType, resolvedCategory, targetPool);

        List<Boolean> recent = recommendationQuestionRepository.findRecentIsCorrectByCategoryAndType(
                userId,
                questionType,
                resolvedCategory,
                PageRequest.of(0, 10));

        double lastTenAccuracy = recent.isEmpty()
                ? 0.0
                : (double) recent.stream().filter(Boolean.TRUE::equals).count() / recent.size();

        List<DifficultyLevel> difficultyPriority = lastTenAccuracy >= 0.8
                ? List.of(DifficultyLevel.ADVANCED, DifficultyLevel.INTERMEDIATE, DifficultyLevel.BEGINNER)
                : lastTenAccuracy >= 0.5
                ? List.of(DifficultyLevel.INTERMEDIATE, DifficultyLevel.BEGINNER, DifficultyLevel.ADVANCED)
                : List.of(DifficultyLevel.BEGINNER, DifficultyLevel.INTERMEDIATE, DifficultyLevel.ADVANCED);

        Set<Long> uniqueIds = new HashSet<>();
        List<com.skillforge.domain.practice.entity.PracticeQuestion> selected = new ArrayList<>();

        for (DifficultyLevel difficulty : difficultyPriority) {
            if (selected.size() >= requestedSize) {
                break;
            }
            var candidates = recommendationQuestionRepository.findUnattemptedByCategoryAndDifficulty(
                    userId,
                    questionType,
                    resolvedCategory,
                    difficulty,
                    PageRequest.of(0, requestedSize * 3));

            for (var candidate : candidates) {
                if (selected.size() >= requestedSize) {
                    break;
                }
                if (uniqueIds.add(candidate.getId())) {
                    selected.add(candidate);
                }
            }
        }

        if (selected.size() < requestedSize) {
            var categoryUnattempted = recommendationQuestionRepository.findAnyUnattemptedByCategory(
                    userId,
                    questionType,
                    resolvedCategory,
                    PageRequest.of(0, requestedSize * 5));

            for (var candidate : categoryUnattempted) {
                if (selected.size() >= requestedSize) {
                    break;
                }
                if (uniqueIds.add(candidate.getId())) {
                    selected.add(candidate);
                }
            }
        }

        if (selected.size() < requestedSize) {
            var categoryActive = recommendationQuestionRepository.findAnyActiveByCategory(
                    questionType,
                    resolvedCategory,
                    PageRequest.of(0, requestedSize * 5));

            for (var candidate : categoryActive) {
                if (selected.size() >= requestedSize) {
                    break;
                }
                if (uniqueIds.add(candidate.getId())) {
                    selected.add(candidate);
                }
            }
        }

        var mapped = selected.stream()
                .limit(requestedSize)
                .map(com.skillforge.domain.practice.dto.PracticeQuestionResponse::from)
                .toList();

        return new NextQuestionSetResponse(
                resolvedCategory,
                requestedSize,
                mapped.size(),
                lastTenAccuracy,
                mapped);
    }
}
