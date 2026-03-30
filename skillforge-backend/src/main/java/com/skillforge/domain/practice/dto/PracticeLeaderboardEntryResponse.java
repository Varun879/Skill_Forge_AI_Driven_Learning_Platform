package com.skillforge.domain.practice.dto;

public class PracticeLeaderboardEntryResponse {

    private final Long userId;
    private final String username;
    private final String displayName;
    private final int rank;
    private final long questionsSolved;
    private final long questionsAttempted;
    private final double accuracy;
    private final double averageSolveTimeSeconds;
    private final int streakDays;
    private final int score;
    private final boolean currentUser;

    public PracticeLeaderboardEntryResponse(
            Long userId,
            String username,
            String displayName,
            int rank,
            long questionsSolved,
            long questionsAttempted,
            double accuracy,
            double averageSolveTimeSeconds,
            int streakDays,
            int score,
            boolean currentUser) {
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.rank = rank;
        this.questionsSolved = questionsSolved;
        this.questionsAttempted = questionsAttempted;
        this.accuracy = accuracy;
        this.averageSolveTimeSeconds = averageSolveTimeSeconds;
        this.streakDays = streakDays;
        this.score = score;
        this.currentUser = currentUser;
    }

    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }
    public int getRank() { return rank; }
    public long getQuestionsSolved() { return questionsSolved; }
    public long getQuestionsAttempted() { return questionsAttempted; }
    public double getAccuracy() { return accuracy; }
    public double getAverageSolveTimeSeconds() { return averageSolveTimeSeconds; }
    public int getStreakDays() { return streakDays; }
    public int getScore() { return score; }
    public boolean isCurrentUser() { return currentUser; }
}
