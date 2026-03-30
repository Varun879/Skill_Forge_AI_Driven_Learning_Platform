package com.skillforge.domain.practice.recommendation.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillforge.common.enums.AptitudeMCQCategory;
import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.common.enums.ProgrammingMCQCategory;
import com.skillforge.domain.practice.entity.UserTopicPerformance;
import com.skillforge.domain.practice.recommendation.dto.CategoryPerformanceDto;
import com.skillforge.domain.practice.recommendation.repository.RecommendationQuestionRepository;
import com.skillforge.domain.practice.repository.UserTopicPerformanceRepository;

/**
 * Reads {@link UserTopicPerformance} rows (keyed by user+category+type) and
 * converts them into {@link CategoryPerformanceDto} objects enriched with the
 * expected solve time from the question catalogue.
 *
 * <p>Rows are upserted lazily at call time to keep performance data fresh
 * based on the existing {@code user_answers} records.  Because the existing
 * {@code PracticeService.submitAnswer()} already maintains {@code topic_mastery}
 * and {@code user_answers}, this analyzer reads from those indirectly via
 * the stored {@code user_topic_performance} values that are kept in sync.</p>
 *
 * <p>Any category the user has <em>never</em> attempted is omitted from the
 * returned list — the selector treats absent entries as "new" topics.</p>
 */
@Service
public class UserPerformanceAnalyzer {

    private final UserTopicPerformanceRepository performanceRepository;
    private final RecommendationQuestionRepository questionRepository;

    public UserPerformanceAnalyzer(
            UserTopicPerformanceRepository performanceRepository,
            RecommendationQuestionRepository questionRepository) {
        this.performanceRepository = performanceRepository;
        this.questionRepository = questionRepository;
    }

    /**
     * Returns performance data for every category the user has attempted
     * for the given question type, ordered from lowest accuracy to highest.
     *
     * @param userId       learner's numeric user ID
     * @param questionType PROGRAMMING_MCQ or APTITUDE_MCQ
     * @return possibly-empty list ordered lowest→highest accuracy
     */
    @Transactional(readOnly = true)
    public List<CategoryPerformanceDto> getPerformance(Long userId, PracticeQuestionType questionType) {
        List<UserTopicPerformance> rows =
                performanceRepository.findByUserIdAndQuestionTypeOrderByAccuracyAsc(userId, questionType);

        List<CategoryPerformanceDto> result = new ArrayList<>();
        for (UserTopicPerformance row : rows) {
            double expectedSecs = resolveExpectedSolveTimeSecs(row.getCategory(), questionType);
            result.add(new CategoryPerformanceDto(
                    row.getCategory(),
                    questionType,
                    row.getNumberOfAttempts(),
                    row.getAccuracy(),
                    row.getAverageSolveTimeSeconds(),
                    expectedSecs,
                    row.getRecentWrongStreak()));
        }
        return result;
    }

    /**
     * Returns performance rows for all defined categories. Categories with no attempts
     * are returned with zeroed metrics so UI charts can render complete axes.
     */
    @Transactional(readOnly = true)
    public List<CategoryPerformanceDto> getPerformanceWithAllCategories(Long userId, PracticeQuestionType questionType) {
        List<CategoryPerformanceDto> attempted = getPerformance(userId, questionType);
        Map<String, CategoryPerformanceDto> attemptedByCategory = attempted.stream()
                .collect(Collectors.toMap(
                        item -> item.getCategory().toLowerCase(),
                        Function.identity(),
                        (existing, ignored) -> existing));

        List<CategoryPerformanceDto> full = new ArrayList<>();
        for (String category : allCategoriesForType(questionType)) {
            CategoryPerformanceDto existing = attemptedByCategory.get(category.toLowerCase());
            if (existing != null) {
                full.add(existing);
            } else {
                full.add(new CategoryPerformanceDto(
                        category,
                        questionType,
                        0,
                        0.0,
                        0.0,
                        resolveExpectedSolveTimeSecs(category, questionType),
                        0));
            }
        }

        return full;
    }

    /**
     * Returns the set of category display-names defined for the given type.
     */
    public List<String> allCategoriesForType(PracticeQuestionType questionType) {
        List<String> cats = new ArrayList<>();
        if (questionType == PracticeQuestionType.PROGRAMMING_MCQ) {
            for (ProgrammingMCQCategory c : ProgrammingMCQCategory.values()) {
                cats.add(c.getDisplayName());
            }
        } else if (questionType == PracticeQuestionType.APTITUDE_MCQ) {
            for (AptitudeMCQCategory c : AptitudeMCQCategory.values()) {
                cats.add(c.getDisplayName());
            }
        }
        return cats;
    }

    /**
     * Upserts a {@link UserTopicPerformance} row after the user answers a
     * question.  Called from
     * {@link NextQuestionRecommendationService} on the same request cycle
     * so performance data stays current.
     *
     * @param userId              learner ID
     * @param category            category name
     * @param questionType        question type
     * @param correct             whether the latest answer was correct
     * @param solveTimeSeconds    time taken on the latest answer
     */
    @Transactional
    public void recordAnswer(
            Long userId,
            String category,
            PracticeQuestionType questionType,
            boolean correct,
            double solveTimeSeconds,
            com.skillforge.domain.user.entity.User user) {

        UserTopicPerformance perf = performanceRepository
                .findByUserIdAndCategoryIgnoreCaseAndQuestionType(userId, category, questionType)
                .orElseGet(() -> {
                    UserTopicPerformance p = new UserTopicPerformance();
                    p.setUser(user);
                    p.setCategory(category);
                    p.setQuestionType(questionType);
                    return p;
                });

        int newAttempts = perf.getNumberOfAttempts() + 1;
        int newCorrect  = perf.getCorrectAttempts() + (correct ? 1 : 0);

        // Incremental rolling average for solve time
        double newAvgTime = perf.getAverageSolveTimeSeconds()
                + (solveTimeSeconds - perf.getAverageSolveTimeSeconds()) / newAttempts;

        int streak = correct ? 0 : Math.min(perf.getRecentWrongStreak() + 1, 3);

        perf.setNumberOfAttempts(newAttempts);
        perf.setCorrectAttempts(newCorrect);
        perf.setAccuracy((double) newCorrect / newAttempts);
        perf.setAverageSolveTimeSeconds(newAvgTime);
        perf.setRecentWrongStreak(streak);

        performanceRepository.save(perf);
    }

    // ------------------------------------------------------------------

    private double resolveExpectedSolveTimeSecs(String category, PracticeQuestionType type) {
        Double avgMinutes = questionRepository.findAvgEstimatedSolveTimeMinutes(category, type);
        return (avgMinutes != null && avgMinutes > 0) ? avgMinutes * 60.0 : 0.0;
    }
}
