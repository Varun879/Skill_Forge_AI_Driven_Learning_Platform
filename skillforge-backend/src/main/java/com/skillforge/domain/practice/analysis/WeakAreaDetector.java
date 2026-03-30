package com.skillforge.domain.practice.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.domain.practice.analysis.dto.TopicPerformance;
import com.skillforge.domain.practice.analysis.dto.WeakAreaResult;
import com.skillforge.domain.practice.analysis.repository.PracticeAnalysisRepository;
import com.skillforge.domain.practice.entity.UserAnswer;

/**
 * Examines a list of per-topic performance snapshots and classifies each topic
 * as <em>weak</em> or <em>not weak</em> based on three independent signals:
 *
 * <ol>
 *   <li><strong>LOW_ACCURACY</strong> — topic accuracy is below 60 %.</li>
 *   <li><strong>SLOW_SOLVE_TIME</strong> — the user's average solve time in
 *       this topic exceeds the estimated solve time for its questions.</li>
 *   <li><strong>CONSECUTIVE_FAILURES</strong> — the last three answers in
 *       this topic were all incorrect.</li>
 * </ol>
 *
 * A topic is marked weak if <em>any</em> of the above conditions is true.
 * This component is read-only and does not modify any data.
 */
@Component
@Transactional(readOnly = true)
public class WeakAreaDetector {

    /** Accuracy ratio below this threshold classifies a topic as weak. */
    static final double ACCURACY_WEAK_THRESHOLD = 0.60;

    /** Number of consecutive failures that triggers the CONSECUTIVE_FAILURES signal. */
    static final int CONSECUTIVE_FAILURE_WINDOW = 3;

    private final PracticeAnalysisRepository analysisRepository;

    public WeakAreaDetector(PracticeAnalysisRepository analysisRepository) {
        this.analysisRepository = analysisRepository;
    }

    /**
     * Evaluates each entry in {@code performances} and returns one
     * {@link WeakAreaResult} per topic.
     *
     * @param userId       the learner's numeric user ID
     * @param type         the question type being analysed
     * @param performances per-topic performance list (from {@link PracticeAnalysisService})
     * @return list of results in the same order as {@code performances}
     */
    public List<WeakAreaResult> detectWeakAreas(
            Long userId,
            PracticeQuestionType type,
            List<TopicPerformance> performances) {

        return performances.stream()
                .map(p -> evaluate(userId, p))
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------
    // private helpers
    // ------------------------------------------------------------------

    private WeakAreaResult evaluate(Long userId, TopicPerformance p) {
        List<String> reasons = new ArrayList<>();

        // Signal 1: low accuracy
        if (p.getAccuracy() < ACCURACY_WEAK_THRESHOLD) {
            reasons.add(WeakAreaResult.REASON_LOW_ACCURACY);
        }

        // Signal 2: slow solve time (only meaningful when estimated time is known)
        if (p.getEstimatedSolveTimeSeconds() > 0
                && p.getAverageSolveTimeSeconds() > p.getEstimatedSolveTimeSeconds()) {
            reasons.add(WeakAreaResult.REASON_SLOW_SOLVE_TIME);
        }

        // Signal 3: consecutive failures in the last N answers for this topic
        List<UserAnswer> recent = analysisRepository.findRecentAnswersByUserTopicAndType(
                userId, p.getTopic(), p.getQuestionType(),
                PageRequest.of(0, CONSECUTIVE_FAILURE_WINDOW));

        if (recent.size() >= CONSECUTIVE_FAILURE_WINDOW
                && recent.stream().noneMatch(ua -> Boolean.TRUE.equals(ua.getIsCorrect()))) {
            reasons.add(WeakAreaResult.REASON_CONSECUTIVE_FAILURES);
        }

        return new WeakAreaResult(p.getTopic(), p.getQuestionType(), !reasons.isEmpty(), reasons);
    }
}
