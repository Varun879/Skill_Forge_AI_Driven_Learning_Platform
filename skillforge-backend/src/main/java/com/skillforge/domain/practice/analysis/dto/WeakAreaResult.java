package com.skillforge.domain.practice.analysis.dto;

import java.util.Collections;
import java.util.List;

import com.skillforge.common.enums.PracticeQuestionType;

/**
 * Result object produced by WeakAreaDetector.
 * Captures whether a topic is classified as weak and the specific reasons.
 */
public class WeakAreaResult {

    /** Reason code: accuracy ratio below the 60 % threshold. */
    public static final String REASON_LOW_ACCURACY = "LOW_ACCURACY";
    /** Reason code: average solve time exceeds the estimated time for the topic. */
    public static final String REASON_SLOW_SOLVE_TIME = "SLOW_SOLVE_TIME";
    /** Reason code: the last three consecutive answers in this topic were all wrong. */
    public static final String REASON_CONSECUTIVE_FAILURES = "CONSECUTIVE_FAILURES";

    private final String topic;
    private final PracticeQuestionType questionType;
    private final boolean weak;
    private final List<String> reasons;

    public WeakAreaResult(
            String topic,
            PracticeQuestionType questionType,
            boolean weak,
            List<String> reasons) {

        this.topic = topic;
        this.questionType = questionType;
        this.weak = weak;
        this.reasons = Collections.unmodifiableList(reasons);
    }

    public String getTopic() { return topic; }
    public PracticeQuestionType getQuestionType() { return questionType; }
    public boolean isWeak() { return weak; }
    public List<String> getReasons() { return reasons; }

    @Override
    public String toString() {
        return "WeakAreaResult{topic='" + topic + "', type=" + questionType
                + ", weak=" + weak + ", reasons=" + reasons + "}";
    }
}
