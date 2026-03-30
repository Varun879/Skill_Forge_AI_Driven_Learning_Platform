package com.skillforge.domain.practice.analysis;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.domain.practice.analysis.dto.TopicPerformance;
import com.skillforge.domain.practice.analysis.repository.PracticeAnalysisRepository;
import com.skillforge.domain.practice.entity.TopicMastery;
import com.skillforge.domain.practice.repository.TopicMasteryRepository;

/**
 * Analyses a learner's historical performance across all practice topics.
 *
 * <p>Data is primarily sourced from the already-maintained {@code topic_mastery}
 * table (kept current by the existing {@code PracticeService}) so that analysis
 * reads are lightweight aggregates rather than full table scans.</p>
 *
 * <p>This service is <em>read-only</em> and does not modify any existing data.</p>
 */
@Service
@Transactional(readOnly = true)
public class PracticeAnalysisService {

    private final TopicMasteryRepository topicMasteryRepository;
    private final PracticeAnalysisRepository analysisRepository;

    public PracticeAnalysisService(
            TopicMasteryRepository topicMasteryRepository,
            PracticeAnalysisRepository analysisRepository) {
        this.topicMasteryRepository = topicMasteryRepository;
        this.analysisRepository = analysisRepository;
    }

    /**
     * Returns a performance snapshot for every topic the user has attempted
     * for the given question type, ordered from lowest mastery score to highest.
     *
     * <p>Each {@link TopicPerformance} entry includes:
     * <ul>
     *   <li>{@code totalAttempts} and {@code correctAttempts} — from {@code topic_mastery}</li>
     *   <li>{@code accuracy} — {@code correctAttempts / totalAttempts} (0 when no attempts)</li>
     *   <li>{@code averageSolveTimeSeconds} — incremental rolling average stored in mastery row</li>
     *   <li>{@code estimatedSolveTimeSeconds} — mean of {@code estimated_solve_time_minutes × 60}
     *       across active questions in that topic</li>
     * </ul>
     * </p>
     *
     * @param userId numeric user identifier (PK in {@code users} table)
     * @param type   the practice question type to filter by
     * @return possibly-empty list of performance snapshots
     */
    public List<TopicPerformance> analyzePerformance(Long userId, PracticeQuestionType type) {
        List<TopicMastery> masteries = topicMasteryRepository
                .findByUserIdOrderByMasteryScoreAsc(userId);

        return masteries.stream()
                .filter(m -> m.getQuestionType() == type)
                .map(m -> buildPerformance(m, type))
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------
    // private helpers
    // ------------------------------------------------------------------

    private TopicPerformance buildPerformance(TopicMastery mastery, PracticeQuestionType type) {
        Double avgEstMinutes = analysisRepository.findAvgEstimatedSolveTimeMinutesForTopic(
                mastery.getTopic(), type);
        double estimatedSecs = avgEstMinutes != null ? avgEstMinutes * 60.0 : 0.0;

        long total = mastery.getAttemptedCount().longValue();
        long correct = mastery.getCorrectCount().longValue();
        double accuracy = total > 0 ? (double) correct / total : 0.0;

        return new TopicPerformance(
                mastery.getTopic(),
                type,
                total,
                correct,
                accuracy,
                mastery.getAvgTimeTakenSeconds().doubleValue(),
                estimatedSecs);
    }
}
