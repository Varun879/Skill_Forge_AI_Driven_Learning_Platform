package com.skillforge.domain.practice.recommendation.dto;

import com.skillforge.common.enums.PracticeQuestionType;

/**
 * Snapshot of a user's performance in a single category, computed by
 * {@link com.skillforge.domain.practice.recommendation.service.UserPerformanceAnalyzer}.
 */
public class CategoryPerformanceDto {

    private final String category;
    private final PracticeQuestionType questionType;
    private final int numberOfAttempts;
    private final double accuracy;
    private final double averageSolveTimeSeconds;
    private final double expectedSolveTimeSeconds;
    private final int recentWrongStreak;

    public CategoryPerformanceDto(
            String category,
            PracticeQuestionType questionType,
            int numberOfAttempts,
            double accuracy,
            double averageSolveTimeSeconds,
            double expectedSolveTimeSeconds,
            int recentWrongStreak) {

        this.category = category;
        this.questionType = questionType;
        this.numberOfAttempts = numberOfAttempts;
        this.accuracy = accuracy;
        this.averageSolveTimeSeconds = averageSolveTimeSeconds;
        this.expectedSolveTimeSeconds = expectedSolveTimeSeconds;
        this.recentWrongStreak = recentWrongStreak;
    }

    public String getCategory() { return category; }
    public PracticeQuestionType getQuestionType() { return questionType; }
    public int getNumberOfAttempts() { return numberOfAttempts; }
    public double getAccuracy() { return accuracy; }
    public double getAverageSolveTimeSeconds() { return averageSolveTimeSeconds; }
    public double getExpectedSolveTimeSeconds() { return expectedSolveTimeSeconds; }
    public int getRecentWrongStreak() { return recentWrongStreak; }

    /**
     * A category is weak when any of the three conditions are true:
     * <ol>
     *   <li>accuracy &lt; 0.60</li>
     *   <li>averageSolveTime &gt; expectedSolveTime (when expected is known)</li>
     *   <li>recentWrongStreak &ge; 3</li>
     * </ol>
     */
    public boolean isWeak() {
        if (accuracy < 0.60) return true;
        if (expectedSolveTimeSeconds > 0 && averageSolveTimeSeconds > expectedSolveTimeSeconds) return true;
        return recentWrongStreak >= 3;
    }

    /**
     * Medium performance: accuracy between 60 % (inclusive) and 80 % (exclusive).
     */
    public boolean isMedium() {
        return accuracy >= 0.60 && accuracy < 0.80;
    }
}
