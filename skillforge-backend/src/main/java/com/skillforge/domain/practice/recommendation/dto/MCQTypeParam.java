package com.skillforge.domain.practice.recommendation.dto;

import com.skillforge.common.enums.PracticeQuestionType;

/**
 * Query parameter enum for /practice/mcq/next.
 *
 * PROGRAMMING -> PROGRAMMING_MCQ
 * APTITUDE    -> APTITUDE_MCQ
 */
public enum MCQTypeParam {
    PROGRAMMING,
    APTITUDE;

    public static MCQTypeParam fromValue(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("type is required");
        }

        String normalized = raw.trim().toUpperCase();
        return switch (normalized) {
            case "PROGRAMMING", "PROGRAMMING_MCQ" -> PROGRAMMING;
            case "APTITUDE", "APTITUDE_MCQ" -> APTITUDE;
            default -> throw new IllegalArgumentException(
                    "Unsupported type '" + raw + "'. Allowed: PROGRAMMING, PROGRAMMING_MCQ, APTITUDE, APTITUDE_MCQ");
        };
    }

    public PracticeQuestionType toPracticeQuestionType() {
        return switch (this) {
            case PROGRAMMING -> PracticeQuestionType.PROGRAMMING_MCQ;
            case APTITUDE -> PracticeQuestionType.APTITUDE_MCQ;
        };
    }
}
