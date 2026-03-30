package com.skillforge.common.enums;

/**
 * Fixed category set for Programming MCQs.
 * The {@code displayName} value is stored verbatim in the {@code topic}
 * column of {@code practice_questions} so that queries can match exactly.
 */
public enum ProgrammingMCQCategory {

    ARRAYS("Arrays"),
    STRINGS("Strings"),
    TIME_COMPLEXITY("Time Complexity"),
    RECURSION("Recursion"),
    DYNAMIC_PROGRAMMING("Dynamic Programming"),
    TREES("Trees"),
    GRAPHS("Graphs"),
    HASHING("Hashing"),
    SORTING("Sorting"),
    SEARCHING("Searching"),
    OBJECT_ORIENTED_PROGRAMMING("Object Oriented Programming"),
    BIT_MANIPULATION("Bit Manipulation");

    private final String displayName;

    ProgrammingMCQCategory(String displayName) {
        this.displayName = displayName;
    }

    /** Returns the human-readable name stored as the {@code topic} in the database. */
    public String getDisplayName() { return displayName; }
}
