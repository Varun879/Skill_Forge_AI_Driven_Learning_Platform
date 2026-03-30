package com.skillforge.domain.practice.recommendation.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillforge.common.enums.AptitudeMCQCategory;
import com.skillforge.common.enums.DifficultyLevel;
import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.common.enums.ProgrammingMCQCategory;
import com.skillforge.domain.practice.entity.AptitudeMCQ;
import com.skillforge.domain.practice.entity.MCQOption;
import com.skillforge.domain.practice.entity.PracticeQuestion;
import com.skillforge.domain.practice.entity.ProgrammingMCQ;
import com.skillforge.domain.practice.recommendation.repository.RecommendationQuestionRepository;
import com.skillforge.domain.practice.repository.AptitudeMCQRepository;
import com.skillforge.domain.practice.repository.MCQOptionRepository;
import com.skillforge.domain.practice.repository.PracticeQuestionRepository;
import com.skillforge.domain.practice.repository.ProgrammingMCQRepository;

/**
 * Auto-populates MCQ data when a type table is empty.
 *
 * Rules implemented:
 * - If table has questions -> no-op
 * - If empty -> generate all categories for that type
 * - 10 questions per category
 * - difficulty distribution per category: 40% EASY, 40% MEDIUM, 20% HARD
 */
@Service
public class MCQGenerationService {

    private static final class GeneratedMcq {
        private final String title;
        private final String prompt;
        private final String explanation;
        private final List<String> options;
        private final int correctIndex;
        private final Set<String> tags;

        private GeneratedMcq(
                String title,
                String prompt,
                String explanation,
                List<String> options,
                int correctIndex,
                Set<String> tags) {
            this.title = title;
            this.prompt = prompt;
            this.explanation = explanation;
            this.options = options;
            this.correctIndex = correctIndex;
            this.tags = tags;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(MCQGenerationService.class);

    private final PracticeQuestionRepository questionRepository;
    private final RecommendationQuestionRepository recommendationQuestionRepository;
    private final ProgrammingMCQRepository programmingMCQRepository;
    private final AptitudeMCQRepository aptitudeMCQRepository;
    private final MCQOptionRepository optionRepository;

    public MCQGenerationService(
            PracticeQuestionRepository questionRepository,
            RecommendationQuestionRepository recommendationQuestionRepository,
            ProgrammingMCQRepository programmingMCQRepository,
            AptitudeMCQRepository aptitudeMCQRepository,
            MCQOptionRepository optionRepository) {
        this.questionRepository = questionRepository;
        this.recommendationQuestionRepository = recommendationQuestionRepository;
        this.programmingMCQRepository = programmingMCQRepository;
        this.aptitudeMCQRepository = aptitudeMCQRepository;
        this.optionRepository = optionRepository;
    }

    @Transactional
    public void populateIfEmpty(PracticeQuestionType type) {
        populateIfBelowMinimum(type, 10);
    }

    @Transactional
    public void populateIfBelowMinimum(PracticeQuestionType type, int minimumPerCategory) {
        int resolvedMinimum = Math.max(1, minimumPerCategory);

        if (type == PracticeQuestionType.PROGRAMMING_MCQ) {
            for (ProgrammingMCQCategory category : ProgrammingMCQCategory.values()) {
                ensureCategoryQuestions(type, category.getDisplayName(), resolvedMinimum);
            }
            return;
        }

        if (type == PracticeQuestionType.APTITUDE_MCQ) {
            for (AptitudeMCQCategory category : AptitudeMCQCategory.values()) {
                ensureCategoryQuestions(type, category.getDisplayName(), resolvedMinimum);
            }
        }
    }

    @Transactional
    public void populateCategoryIfBelowMinimum(PracticeQuestionType type, String category, int minimumForCategory) {
        if (type != PracticeQuestionType.PROGRAMMING_MCQ && type != PracticeQuestionType.APTITUDE_MCQ) {
            return;
        }
        if (category == null || category.isBlank()) {
            return;
        }
        ensureCategoryQuestions(type, category.trim(), Math.max(1, minimumForCategory));
    }

    private void ensureCategoryQuestions(PracticeQuestionType type, String category, int minimumPerCategory) {
        long currentCount = recommendationQuestionRepository.countByCategoryAndType(category, type);
        if (currentCount >= minimumPerCategory) {
            return;
        }

        int needed = (int) (minimumPerCategory - currentCount);
        int generateCount = Math.min(20, Math.max(10, needed));
        log.info("Generating {} {} questions for category '{}' (current={}, min={})",
                generateCount, type, category, currentCount, minimumPerCategory);
        generateForCategory(type, category, generateCount, (int) currentCount);
    }

    private void generateForCategory(PracticeQuestionType type, String category, int questionCount, int existingCount) {
        List<DifficultyLevel> distribution = buildDistribution(questionCount);

        for (int i = 0; i < distribution.size(); i++) {
            DifficultyLevel difficulty = distribution.get(i);
            int ordinal = existingCount + i + 1;
            GeneratedMcq generated = buildGeneratedMcq(type, category, ordinal, difficulty);

            PracticeQuestion question = new PracticeQuestion();
            question.setQuestionType(type);
            question.setTitle(generated.title);
            question.setPrompt(generated.prompt);
            question.setTopic(category);
            question.setTags(generated.tags);
            question.setDifficultyLevel(difficulty);
            question.setEstimatedSolveTimeMinutes(estimatedMinutes(difficulty));
            question.setSuccessRate(BigDecimal.ZERO);
            question.setIsActive(true);
            question = questionRepository.save(question);

            if (type == PracticeQuestionType.PROGRAMMING_MCQ) {
                ProgrammingMCQ ext = new ProgrammingMCQ();
                ext.setQuestion(question);
                ext.setExplanation(generated.explanation);
                programmingMCQRepository.save(ext);
            } else {
                AptitudeMCQ ext = new AptitudeMCQ();
                ext.setQuestion(question);
                ext.setExplanation(generated.explanation);
                aptitudeMCQRepository.save(ext);
            }

            saveOptions(question, generated.options, generated.correctIndex);
        }
    }

    private List<DifficultyLevel> buildDistribution(int questionCount) {
        int beginnerCount = Math.round(questionCount * 0.4f);
        int intermediateCount = Math.round(questionCount * 0.4f);
        int advancedCount = questionCount - beginnerCount - intermediateCount;

        List<DifficultyLevel> distribution = new ArrayList<>(questionCount);
        for (int i = 0; i < beginnerCount; i++) {
            distribution.add(DifficultyLevel.BEGINNER);
        }
        for (int i = 0; i < intermediateCount; i++) {
            distribution.add(DifficultyLevel.INTERMEDIATE);
        }
        for (int i = 0; i < advancedCount; i++) {
            distribution.add(DifficultyLevel.ADVANCED);
        }
        return distribution;
    }

    private void saveOptions(PracticeQuestion question, List<String> options, int correctIndex) {
        for (int i = 0; i < options.size(); i++) {
            MCQOption option = new MCQOption();
            option.setQuestion(question);
            option.setOptionText(options.get(i));
            option.setDisplayOrder(i + 1);
            option.setIsCorrect(i == correctIndex);
            optionRepository.save(option);
        }
    }

    private GeneratedMcq buildGeneratedMcq(
            PracticeQuestionType type,
            String category,
            int idx,
            DifficultyLevel difficulty) {

        String correct = correctStatement(type, category, difficulty);
        List<String> distractors = distractorStatements(type, category, difficulty);
        int correctIndex = Math.floorMod(idx, 4);

        List<String> options = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            if (i == correctIndex) {
                options.add(correct);
            } else {
                int distractorIndex = i < correctIndex ? i : i - 1;
                options.add(distractors.get(distractorIndex));
            }
        }

        String titlePrefix = type == PracticeQuestionType.PROGRAMMING_MCQ ? "Programming" : "Aptitude";
        String title = titlePrefix + " • " + category + " • Q" + idx;
        String prompt = buildPrompt(type, category, difficulty, idx);
        String explanation = "Correct answer: " + correct
                + " This aligns with standard " + category + " reasoning for " + difficulty.name().toLowerCase() + " level practice.";

        Set<String> tags = new LinkedHashSet<>();
        tags.add(slug(category));
        tags.add(type == PracticeQuestionType.PROGRAMMING_MCQ ? "programming-mcq" : "aptitude-mcq");
        tags.add("auto-generated");

        return new GeneratedMcq(title, prompt, explanation, options, correctIndex, tags);
    }

    private String buildPrompt(PracticeQuestionType type, String category, DifficultyLevel difficulty, int idx) {
        String difficultyPhrase = switch (difficulty) {
            case BEGINNER -> "basic concept";
            case INTERMEDIATE -> "application-oriented";
            case ADVANCED -> "edge-case and optimization";
        };

        if (type == PracticeQuestionType.PROGRAMMING_MCQ) {
            return "[" + category + "] For this " + difficultyPhrase + " programming scenario (Q" + idx + "), choose the most accurate statement.";
        }
        return "[" + category + "] For this " + difficultyPhrase + " aptitude scenario (Q" + idx + "), choose the most accurate statement.";
    }

    private String correctStatement(PracticeQuestionType type, String category, DifficultyLevel difficulty) {
        if (type == PracticeQuestionType.PROGRAMMING_MCQ) {
            return switch (category) {
                case "Arrays" -> difficulty == DifficultyLevel.ADVANCED
                        ? "Sliding window or prefix-sum patterns can reduce repeated range work from O(n^2) to near O(n)."
                        : "Arrays provide O(1) index access when position is known.";
                case "Strings" -> "Two-pointer and frequency-map techniques are common for efficient substring and anagram checks.";
                case "Time Complexity" -> "Big-O focuses on growth rate as input size increases, not machine-specific runtime constants.";
                case "Recursion" -> "Every recursive solution needs a base case that guarantees termination.";
                case "Dynamic Programming" -> "DP works when the problem has overlapping subproblems and optimal substructure.";
                case "Trees" -> "In a BST, inorder traversal yields values in sorted order.";
                case "Graphs" -> "BFS finds shortest path length in an unweighted graph.";
                case "Hashing" -> "Hash maps offer expected O(1) insert and lookup, with collisions handled internally.";
                case "Sorting" -> "Merge sort guarantees O(n log n) time in worst case.";
                case "Searching" -> "Binary search requires the data to be sorted before searching.";
                case "Object Oriented Programming" -> "Polymorphism lets a base reference call overridden behavior in derived classes.";
                case "Bit Manipulation" -> "n & (n - 1) clears the lowest set bit of a positive integer.";
                default -> "Choose the option that preserves correctness while improving clarity and complexity for the given context.";
            };
        }

        return switch (category) {
            case "Percentages" -> "Percentage change uses (difference/base) × 100 with the original value as base.";
            case "Probability" -> "For equally likely outcomes, probability = favorable outcomes / total outcomes.";
            case "Permutations and Combinations" -> "Use permutations when arrangement order matters; combinations when it does not.";
            case "Number Systems" -> "Divisibility tests and modular arithmetic help solve number-system questions quickly.";
            case "Logical Reasoning" -> "Validate each condition step by step before drawing the final logical conclusion.";
            case "Time and Work" -> "Combined work rates add: total rate = individual rate1 + individual rate2.";
            case "Speed Distance Time" -> "Distance = speed × time and average speed requires total distance / total time.";
            case "Ratios and Proportions" -> "A proportion is valid when cross-products are equal.";
            case "Profit and Loss" -> "Profit or loss percentage is always computed on cost price unless stated otherwise.";
            case "Averages" -> "Average = total sum / count; update by adjusting sum first, then divide.";
            case "Data Interpretation" -> "Read units and labels first, then compare values before calculating percentages or trends.";
            default -> "Choose the option based on the standard formula and validate units before finalizing.";
        };
    }

    private List<String> distractorStatements(PracticeQuestionType type, String category, DifficultyLevel difficulty) {
        if (type == PracticeQuestionType.PROGRAMMING_MCQ) {
            return List.of(
                    "It is always best to choose the most complex algorithm to avoid edge cases.",
                    "Correctness can be ignored if average runtime appears fast on small samples.",
                    "Sorting and hashing behave the same for all problem constraints.");
        }

        return List.of(
                "The shortest calculation is always the correct one regardless of assumptions.",
                "Approximation is preferred even when exact values are required in the question.",
                "Units and base values do not affect aptitude outcomes if ratios look similar.");
    }

    private String slug(String category) {
        return category.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }

    private int estimatedMinutes(DifficultyLevel difficulty) {
        return switch (difficulty) {
            case BEGINNER -> 1;
            case INTERMEDIATE -> 2;
            case ADVANCED -> 3;
        };
    }
}
