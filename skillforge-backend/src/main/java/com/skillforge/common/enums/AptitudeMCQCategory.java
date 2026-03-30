package com.skillforge.common.enums;

/**
 * Fixed category set for Aptitude MCQs.
 * The {@code displayName} value is stored verbatim in the {@code topic}
 * column of {@code practice_questions} so that queries can match exactly.
 */
public enum AptitudeMCQCategory {

    PERCENTAGES("Percentages"),
    PROBABILITY("Probability"),
    PERMUTATIONS_AND_COMBINATIONS("Permutations and Combinations"),
    NUMBER_SYSTEMS("Number Systems"),
    LOGICAL_REASONING("Logical Reasoning"),
    TIME_AND_WORK("Time and Work"),
    SPEED_DISTANCE_TIME("Speed Distance Time"),
    RATIOS_AND_PROPORTIONS("Ratios and Proportions"),
    PROFIT_AND_LOSS("Profit and Loss"),
    AVERAGES("Averages"),
    DATA_INTERPRETATION("Data Interpretation");

    private final String displayName;

    AptitudeMCQCategory(String displayName) {
        this.displayName = displayName;
    }

    /** Returns the human-readable name stored as the {@code topic} in the database. */
    public String getDisplayName() { return displayName; }
}
