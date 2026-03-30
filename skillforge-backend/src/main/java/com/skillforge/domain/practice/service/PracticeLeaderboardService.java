package com.skillforge.domain.practice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillforge.domain.practice.dto.PracticeLeaderboardEntryResponse;
import com.skillforge.domain.practice.dto.PracticeLeaderboardResponse;
import com.skillforge.domain.practice.entity.PracticeStats;
import com.skillforge.domain.practice.entity.UserAnswer;
import com.skillforge.domain.practice.repository.PracticeStatsRepository;
import com.skillforge.domain.practice.repository.UserAnswerRepository;
import com.skillforge.domain.user.entity.User;

@Service
public class PracticeLeaderboardService {

    private final PracticeStatsRepository practiceStatsRepository;
    private final UserAnswerRepository userAnswerRepository;

    public PracticeLeaderboardService(
            PracticeStatsRepository practiceStatsRepository,
            UserAnswerRepository userAnswerRepository) {
        this.practiceStatsRepository = practiceStatsRepository;
        this.userAnswerRepository = userAnswerRepository;
    }

    @Transactional(readOnly = true)
    public PracticeLeaderboardResponse getPracticeLeaderboard(String currentUserEmail, int limit) {
        List<PracticeStats> stats = practiceStatsRepository.findAll();
        if (stats.isEmpty()) {
            return new PracticeLeaderboardResponse(LocalDateTime.now(), 0, List.of());
        }

        Map<Long, AggregatedStats> aggregatedByUser = new HashMap<>();
        for (PracticeStats stat : stats) {
            User user = stat.getUser();
            AggregatedStats aggregate = aggregatedByUser.computeIfAbsent(user.getId(), id -> new AggregatedStats(user));
            Integer totalAttempted = stat.getTotalAttempted();
            Integer totalCorrect = stat.getTotalCorrect();
            Long totalTimeTakenSeconds = stat.getTotalTimeTakenSeconds();
            aggregate.questionsAttempted += totalAttempted == null ? 0 : totalAttempted;
            aggregate.questionsSolved += totalCorrect == null ? 0 : totalCorrect;
            aggregate.totalTimeTakenSeconds += totalTimeTakenSeconds == null ? 0 : totalTimeTakenSeconds;
        }

        Map<Long, Integer> streakByUser = buildStreakMap();
        for (Map.Entry<Long, AggregatedStats> entry : aggregatedByUser.entrySet()) {
            AggregatedStats aggregate = entry.getValue();
            aggregate.streakDays = streakByUser.getOrDefault(entry.getKey(), 0);
            aggregate.accuracy = percentage(aggregate.questionsSolved, aggregate.questionsAttempted).doubleValue();
            aggregate.averageSolveTimeSeconds = aggregate.questionsAttempted == 0
                    ? 0.0
                    : round((double) aggregate.totalTimeTakenSeconds / aggregate.questionsAttempted, 2);
            aggregate.score = computeScore(aggregate.questionsSolved, aggregate.accuracy, aggregate.streakDays, aggregate.averageSolveTimeSeconds);
        }

        List<AggregatedStats> sorted = new ArrayList<>(aggregatedByUser.values());
        sorted.sort(Comparator
                .comparingInt(AggregatedStats::getScore).reversed()
                .thenComparingLong(AggregatedStats::getQuestionsSolved).reversed()
                .thenComparingDouble(AggregatedStats::getAccuracy).reversed());

        int normalizedLimit = Math.max(1, Math.min(limit, 100));
        List<PracticeLeaderboardEntryResponse> entries = new ArrayList<>();
        int rank = 1;
        for (AggregatedStats aggregate : sorted) {
            if (entries.size() >= normalizedLimit) {
                break;
            }

            User user = aggregate.user;
            String displayName = (user.getFirstName() + " " + user.getLastName()).trim();
            entries.add(new PracticeLeaderboardEntryResponse(
                    user.getId(),
                    user.getUsername(),
                    displayName.isBlank() ? user.getUsername() : displayName,
                    rank,
                    aggregate.questionsSolved,
                    aggregate.questionsAttempted,
                    aggregate.accuracy,
                    aggregate.averageSolveTimeSeconds,
                    aggregate.streakDays,
                    aggregate.score,
                    currentUserEmail != null && currentUserEmail.equalsIgnoreCase(user.getEmail())));
            rank++;
        }

        return new PracticeLeaderboardResponse(LocalDateTime.now(), sorted.size(), entries);
    }

    private Map<Long, Integer> buildStreakMap() {
        List<UserAnswer> answers = userAnswerRepository.findAllByOrderByCreatedAtDesc();
        Map<Long, Set<LocalDate>> activeDatesByUser = new HashMap<>();

        for (UserAnswer answer : answers) {
            Long userId = answer.getUser().getId();
            LocalDate attemptDate = answer.getCreatedAt().toLocalDate();
            activeDatesByUser.computeIfAbsent(userId, id -> new HashSet<>()).add(attemptDate);
        }

        Map<Long, Integer> streakMap = new HashMap<>();
        for (Map.Entry<Long, Set<LocalDate>> entry : activeDatesByUser.entrySet()) {
            streakMap.put(entry.getKey(), computeStreak(entry.getValue()));
        }
        return streakMap;
    }

    private int computeStreak(Set<LocalDate> activityDates) {
        if (activityDates.isEmpty()) {
            return 0;
        }

        LocalDate cursor = activityDates.stream().max(LocalDate::compareTo).orElse(LocalDate.now());
        int streak = 0;
        while (activityDates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private BigDecimal percentage(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private int computeScore(long solved, double accuracy, int streakDays, double averageSolveTimeSeconds) {
        double rawScore =
                (solved * 12.0)
                + (accuracy * 4.0)
                + (streakDays * 10.0)
                - (averageSolveTimeSeconds * 0.2);
        return Math.max(0, (int) Math.round(rawScore));
    }

    private double round(double value, int scale) {
        return BigDecimal.valueOf(value)
                .setScale(scale, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static class AggregatedStats {
        private final User user;
        private long questionsSolved;
        private long questionsAttempted;
        private long totalTimeTakenSeconds;
        private int streakDays;
        private double accuracy;
        private double averageSolveTimeSeconds;
        private int score;

        private AggregatedStats(User user) {
            this.user = user;
        }

        private long getQuestionsSolved() {
            return questionsSolved;
        }

        private double getAccuracy() {
            return accuracy;
        }

        private int getScore() {
            return score;
        }
    }
}
