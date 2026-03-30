package com.skillforge.domain.practice.analysis;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.skillforge.common.enums.DifficultyLevel;
import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.domain.practice.analysis.dto.TopicPerformance;
import com.skillforge.domain.practice.analysis.dto.WeakAreaResult;
import com.skillforge.domain.practice.analysis.repository.PracticeAnalysisRepository;
import com.skillforge.domain.practice.entity.PracticeQuestion;
import com.skillforge.domain.practice.entity.UserAnswer;
import com.skillforge.domain.practice.repository.PracticeQuestionRepository;

/**
 * Orchestrates the selection of the next practice question for a learner.
 *
 * <h3>Selection priority</h3>
 * <ol>
 *   <li>Questions from <em>weak</em> topics (as detected by {@link WeakAreaDetector})</li>
 *   <li>Questions from <em>medium-performance</em> topics (60 %–80 % accuracy)</li>
 *   <li>Questions from <em>new</em> topics the user has never attempted</li>
 *   <li>Global fallback — any active question of the requested type</li>
 * </ol>
 *
 * <h3>Difficulty adjustment</h3>
 * For each candidate topic the difficulty level is inferred from the user's
 * most recent answer in that topic:
 * <ul>
 *   <li>Answered <em>correctly</em> <strong>and</strong> faster than the
 *       topic's estimated solve time → increase difficulty (BEGINNER → INTERMEDIATE → ADVANCED)</li>
 *   <li>Answered <em>incorrectly</em> or slower than estimated → decrease difficulty</li>
 *   <li>No prior answers → start at BEGINNER</li>
 * </ul>
 *
 * This component is read-only and does not modify any data.
 */
@Component
@Transactional(readOnly = true)
public class NextQuestionSelector {

    private static final Logger log = LoggerFactory.getLogger(NextQuestionSelector.class);

    /** Lower bound of "medium" accuracy band (inclusive). */
    private static final double MEDIUM_ACCURACY_LOW = 0.60;
    /** Upper bound of "medium" accuracy band (exclusive). */
    private static final double MEDIUM_ACCURACY_HIGH = 0.80;

    private final PracticeAnalysisService analysisService;
    private final WeakAreaDetector weakAreaDetector;
    private final PracticeQuestionService practiceQuestionService;
    private final PracticeAnalysisRepository analysisRepository;
    private final PracticeQuestionRepository questionRepository;

    public NextQuestionSelector(
            PracticeAnalysisService analysisService,
            WeakAreaDetector weakAreaDetector,
            PracticeQuestionService practiceQuestionService,
            PracticeAnalysisRepository analysisRepository,
            PracticeQuestionRepository questionRepository) {
        this.analysisService = analysisService;
        this.weakAreaDetector = weakAreaDetector;
        this.practiceQuestionService = practiceQuestionService;
        this.analysisRepository = analysisRepository;
        this.questionRepository = questionRepository;
    }

    /**
     * Selects the most appropriate next question for the given user and
     * question type following the priority and difficulty-adjustment rules
     * described above.
     *
     * @param userId numeric user ID
     * @param type   the practice question type (PROGRAMMING_MCQ or APTITUDE_MCQ)
     * @return the selected question, or {@link Optional#empty()} if no question
     *         is available at all for the type
     */
    public Optional<PracticeQuestion> selectNext(Long userId, PracticeQuestionType type) {
        List<TopicPerformance> performances = analysisService.analyzePerformance(userId, type);
        List<WeakAreaResult> weakAreas = weakAreaDetector.detectWeakAreas(userId, type, performances);

        // Priority 1 — weak topics
        for (WeakAreaResult weak : weakAreas) {
            if (weak.isWeak()) {
                Optional<PracticeQuestion> q = pickFromTopic(userId, type, weak.getTopic());
                if (q.isPresent()) {
                    log.debug("Selected question from WEAK topic '{}' for user {}", weak.getTopic(), userId);
                    return q;
                }
            }
        }

        // Priority 2 — medium-performance topics (60 %–80 % accuracy)
        for (TopicPerformance p : performances) {
            if (p.getAccuracy() >= MEDIUM_ACCURACY_LOW && p.getAccuracy() < MEDIUM_ACCURACY_HIGH) {
                Optional<PracticeQuestion> q = pickFromTopic(userId, type, p.getTopic());
                if (q.isPresent()) {
                    log.debug("Selected question from MEDIUM topic '{}' for user {}", p.getTopic(), userId);
                    return q;
                }
            }
        }

        // Priority 3 — new (unattempted) topics
        Set<String> attemptedTopics = buildAttemptedTopicSet(performances);
        List<String> allTopics = analysisRepository.findAllActiveTopicsByType(type);
        for (String topic : allTopics) {
            if (!attemptedTopics.contains(topic.toLowerCase())) {
                Optional<PracticeQuestion> q = practiceQuestionService
                        .fetchUnattemptedForTopicDifficulty(userId, type, topic, DifficultyLevel.BEGINNER);
                if (q.isPresent()) {
                    log.debug("Selected question from NEW topic '{}' for user {}", topic, userId);
                    return q;
                }
            }
        }

        // Priority 4 — global fallback (any active question)
        List<PracticeQuestion> fallback = questionRepository.findFallbackRecommendation(
                type, null, PageRequest.of(0, 1));
        if (!fallback.isEmpty()) {
            log.debug("Selected fallback question for user {} type {}", userId, type);
            return Optional.of(fallback.get(0));
        }

        log.warn("No question could be selected for user {} type {}", userId, type);
        return Optional.empty();
    }

    // ------------------------------------------------------------------
    // private helpers
    // ------------------------------------------------------------------

    /** Picks an unattempted question from a specific topic, adjusting difficulty. */
    private Optional<PracticeQuestion> pickFromTopic(Long userId, PracticeQuestionType type, String topic) {
        DifficultyLevel difficulty = determineDifficultyForTopic(userId, type, topic);
        return practiceQuestionService.fetchUnattemptedForTopicDifficulty(userId, type, topic, difficulty);
    }

    /**
     * Determines the appropriate difficulty level for the next question in a topic
     * by inspecting the user's most recent answer in that topic.
     */
    private DifficultyLevel determineDifficultyForTopic(
            Long userId, PracticeQuestionType type, String topic) {

        List<UserAnswer> recent = analysisRepository.findRecentAnswersByUserTopicAndType(
                userId, topic, type, PageRequest.of(0, 1));

        if (recent.isEmpty()) {
            return DifficultyLevel.BEGINNER;
        }

        UserAnswer last = recent.get(0);
        DifficultyLevel current = last.getQuestion().getDifficultyLevel();

        Double avgEstMinutes = analysisRepository.findAvgEstimatedSolveTimeMinutesForTopic(topic, type);
        // If the estimate is unknown treat as infinitely long — never penalise for being slow
        double estimatedSecs = (avgEstMinutes != null && avgEstMinutes > 0)
                ? avgEstMinutes * 60.0
                : Double.MAX_VALUE;

        boolean correct = Boolean.TRUE.equals(last.getIsCorrect());
        boolean faster = last.getTimeTakenSeconds() < estimatedSecs;

        if (correct && faster) {
            return increase(current);
        }
        if (!correct || !faster) {
            return decrease(current);
        }
        return current;
    }

    private Set<String> buildAttemptedTopicSet(List<TopicPerformance> performances) {
        Set<String> set = new HashSet<>();
        for (TopicPerformance p : performances) {
            set.add(p.getTopic().toLowerCase());
        }
        return set;
    }

    private DifficultyLevel increase(DifficultyLevel d) {
        return switch (d) {
            case BEGINNER -> DifficultyLevel.INTERMEDIATE;
            case INTERMEDIATE -> DifficultyLevel.ADVANCED;
            case ADVANCED -> DifficultyLevel.ADVANCED;
        };
    }

    private DifficultyLevel decrease(DifficultyLevel d) {
        return switch (d) {
            case ADVANCED -> DifficultyLevel.INTERMEDIATE;
            case INTERMEDIATE -> DifficultyLevel.BEGINNER;
            case BEGINNER -> DifficultyLevel.BEGINNER;
        };
    }
}
