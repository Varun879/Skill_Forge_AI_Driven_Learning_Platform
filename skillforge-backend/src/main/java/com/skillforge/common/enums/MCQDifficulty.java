package com.skillforge.common.enums;

/**
 * Difficulty levels used in AI-generated MCQ prompts and surface-level
 * category APIs.  These map to the existing {@link DifficultyLevel} enum
 * which is stored in the database.
 */
public enum MCQDifficulty {

    EASY, MEDIUM, HARD;

    /** Convert this difficulty to the persisted {@link DifficultyLevel} enum. */
    public DifficultyLevel toDifficultyLevel() {
        return switch (this) {
            case EASY   -> DifficultyLevel.BEGINNER;
            case MEDIUM -> DifficultyLevel.INTERMEDIATE;
            case HARD   -> DifficultyLevel.ADVANCED;
        };
    }

    /** Convert from a persisted {@link DifficultyLevel} to {@link MCQDifficulty}. */
    public static MCQDifficulty fromDifficultyLevel(DifficultyLevel level) {
        return switch (level) {
            case BEGINNER     -> EASY;
            case INTERMEDIATE -> MEDIUM;
            case ADVANCED     -> HARD;
        };
    }

    /** Case-insensitive parse; falls back to {@code MEDIUM} for unknown values. */
    public static MCQDifficulty parseOrDefault(String raw) {
        if (raw == null) return MEDIUM;
        return switch (raw.trim().toUpperCase()) {
            case "EASY"   -> EASY;
            case "HARD"   -> HARD;
            default       -> MEDIUM;
        };
    }
}
