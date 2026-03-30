package com.skillforge.domain.practice.analysis.dto;

import com.skillforge.common.enums.PracticeQuestionType;

/**
 * Immutable snapshot of a user's performance in a single topic.
 * Computed by PracticeAnalysisService from TopicMastery aggregates.
 */
public class TopicPerformance {

    private final String topic;
    private final PracticeQuestionType questionType;
    private final long totalAttempts;
    private final long correctAttempts;
    /** Ratio in [0.0, 1.0]. Zero when no attempts exist. */
    private final double accuracy;
    /** Mean solve time observed across all user attempts (seconds). */
    private final double averageSolveTimeSeconds;
    /** Mean estimated solve time for questions in this topic (seconds). */
    private final double estimatedSolveTimeSeconds;

    public TopicPerformance(
            String topic,
            PracticeQuestionType questionType,
            long totalAttempts,
            long correctAttempts,
            double accuracy,
            double averageSolveTimeSeconds,
            double estimatedSolveTimeSeconds) {

        this.topic = topic;
        this.questionType = questionType;
        this.totalAttempts = totalAttempts;
        this.correctAttempts = correctAttempts;
        this.accuracy = accuracy;
        this.averageSolveTimeSeconds = averageSolveTimeSeconds;
        this.estimatedSolveTimeSeconds = estimatedSolveTimeSeconds;
    }

    public String getTopic() { return topic; }
    public PracticeQuestionType getQuestionType() { return questionType; }
    public long getTotalAttempts() { return totalAttempts; }
    public long getCorrectAttempts() { return correctAttempts; }
    public double getAccuracy() { return accuracy; }
    public double getAverageSolveTimeSeconds() { return averageSolveTimeSeconds; }
    public double getEstimatedSolveTimeSeconds() { return estimatedSolveTimeSeconds; }

    @Override
    public String toString() {
        return "TopicPerformance{topic='" + topic + "', type=" + questionType
                + ", accuracy=" + String.format("%.2f", accuracy)
                + ", avgTime=" + averageSolveTimeSeconds + "s}";
    }
}
