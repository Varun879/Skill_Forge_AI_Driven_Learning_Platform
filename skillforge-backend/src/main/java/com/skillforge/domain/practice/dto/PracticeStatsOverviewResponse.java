package com.skillforge.domain.practice.dto;

public class PracticeStatsOverviewResponse {

    private long attempted;
    private long correct;
    private double accuracy;
    private double averageTimeTakenSeconds;

    public PracticeStatsOverviewResponse(long attempted, long correct, double accuracy, double averageTimeTakenSeconds) {
        this.attempted = attempted;
        this.correct = correct;
        this.accuracy = accuracy;
        this.averageTimeTakenSeconds = averageTimeTakenSeconds;
    }

    public long getAttempted() { return attempted; }
    public long getCorrect() { return correct; }
    public double getAccuracy() { return accuracy; }
    public double getAverageTimeTakenSeconds() { return averageTimeTakenSeconds; }
}
