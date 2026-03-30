package com.skillforge.domain.practice.service;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.skillforge.common.enums.DifficultyLevel;
import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.domain.practice.entity.AptitudeMCQ;
import com.skillforge.domain.practice.entity.MCQOption;
import com.skillforge.domain.practice.entity.PracticeQuestion;
import com.skillforge.domain.practice.entity.ProgrammingMCQ;
import com.skillforge.domain.practice.repository.AptitudeMCQRepository;
import com.skillforge.domain.practice.repository.MCQOptionRepository;
import com.skillforge.domain.practice.repository.PracticeQuestionRepository;
import com.skillforge.domain.practice.repository.ProgrammingMCQRepository;

@Component
public class PracticeDataSeeder implements ApplicationRunner {

    private final PracticeQuestionRepository practiceQuestionRepository;
    private final ProgrammingMCQRepository programmingMCQRepository;
    private final AptitudeMCQRepository aptitudeMCQRepository;
    private final MCQOptionRepository mcqOptionRepository;

    public PracticeDataSeeder(
            PracticeQuestionRepository practiceQuestionRepository,
            ProgrammingMCQRepository programmingMCQRepository,
            AptitudeMCQRepository aptitudeMCQRepository,
            MCQOptionRepository mcqOptionRepository) {
        this.practiceQuestionRepository = practiceQuestionRepository;
        this.programmingMCQRepository = programmingMCQRepository;
        this.aptitudeMCQRepository = aptitudeMCQRepository;
        this.mcqOptionRepository = mcqOptionRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (practiceQuestionRepository.count() > 0) {
            return;
        }

        seedProgrammingMcqs();
        seedAptitudeMcqs();
    }

    private void seedProgrammingMcqs() {
        createMcqQuestion(
                PracticeQuestionType.PROGRAMMING_MCQ,
                "Array lookup complexity",
                "What is the average time complexity of accessing an element in an array by index?",
                DifficultyLevel.BEGINNER,
                "Data Structures",
                List.of("arrays", "complexity", "basics"),
                2,
                "Direct index access is constant time because the address offset can be computed immediately.",
                68.0,
                List.of(
                        option("O(1)", true),
                        option("O(log n)", false),
                        option("O(n)", false),
                        option("O(n log n)", false)));

        createMcqQuestion(
                PracticeQuestionType.PROGRAMMING_MCQ,
                "Java inheritance keyword",
                "Which keyword is used in Java to inherit a class?",
                DifficultyLevel.BEGINNER,
                "Java OOP",
                List.of("java", "oop", "inheritance"),
                2,
                "A Java class inherits from another class using the extends keyword.",
                74.0,
                List.of(
                        option("implements", false),
                        option("inherits", false),
                        option("extends", true),
                        option("super", false)));

        createMcqQuestion(
                PracticeQuestionType.PROGRAMMING_MCQ,
                "SQL filtering clause",
                "Which SQL clause is used to filter rows before grouping?",
                DifficultyLevel.INTERMEDIATE,
                "SQL",
                List.of("sql", "database", "querying"),
                3,
                "WHERE filters rows before GROUP BY is applied. HAVING filters after grouping.",
                57.0,
                List.of(
                        option("HAVING", false),
                        option("WHERE", true),
                        option("ORDER BY", false),
                        option("LIMIT", false)));

        createMcqQuestion(
                PracticeQuestionType.PROGRAMMING_MCQ,
                "Recursion base case",
                "Why is a base case necessary in recursion?",
                DifficultyLevel.INTERMEDIATE,
                "Algorithms",
                List.of("recursion", "algorithms", "functions"),
                4,
                "Without a base case, the recursive calls would continue indefinitely until stack overflow.",
                49.0,
                List.of(
                        option("To make code run faster in all cases", false),
                        option("To stop recursive calls from continuing forever", true),
                        option("To allocate heap memory", false),
                        option("To replace loops automatically", false)));

        createMcqQuestion(
                PracticeQuestionType.PROGRAMMING_MCQ,
                "JavaScript event loop",
                "What does the JavaScript event loop primarily do?",
                DifficultyLevel.ADVANCED,
                "JavaScript Runtime",
                List.of("javascript", "async", "event-loop"),
                5,
                "The event loop coordinates the call stack and task queues so asynchronous callbacks run when the stack is clear.",
                41.0,
                List.of(
                        option("Compiles JavaScript to bytecode", false),
                        option("Schedules asynchronous callbacks when the call stack is free", true),
                        option("Stores variables in closure scope", false),
                        option("Creates new threads for every promise", false)));

        createMcqQuestion(
                PracticeQuestionType.PROGRAMMING_MCQ,
                "Binary search prerequisite",
                "What must be true before binary search can be correctly applied to a collection?",
                DifficultyLevel.INTERMEDIATE,
                "Searching",
                List.of("binary-search", "algorithms", "sorting"),
                3,
                "Binary search requires the data to be sorted according to the comparison being used.",
                53.0,
                List.of(
                        option("The collection must be sorted", true),
                        option("The collection must contain unique values", false),
                        option("The collection must be stored in a linked list", false),
                        option("The collection size must be a power of two", false)));
    }

    private void seedAptitudeMcqs() {
        createMcqQuestion(
                PracticeQuestionType.APTITUDE_MCQ,
                "Percentage increase",
                "A value rises from 80 to 100. What is the percentage increase?",
                DifficultyLevel.BEGINNER,
                "Percentages",
                List.of("percentages", "arithmetic", "growth"),
                2,
                "Increase is 20 on a base of 80, so percentage increase is (20/80) x 100 = 25%.",
                71.0,
                List.of(
                        option("20%", false),
                        option("25%", true),
                        option("30%", false),
                        option("40%", false)));

        createMcqQuestion(
                PracticeQuestionType.APTITUDE_MCQ,
                "Ratio simplification",
                "Simplify the ratio 18:24.",
                DifficultyLevel.BEGINNER,
                "Ratios",
                List.of("ratios", "simplification", "basics"),
                2,
                "Both terms divide by 6, giving 3:4.",
                78.0,
                List.of(
                        option("2:3", false),
                        option("3:4", true),
                        option("4:3", false),
                        option("6:8", false)));

        createMcqQuestion(
                PracticeQuestionType.APTITUDE_MCQ,
                "Time and work",
                "If one worker completes a job in 6 days, what fraction of the job is done in 1 day?",
                DifficultyLevel.BEGINNER,
                "Time and Work",
                List.of("time-and-work", "fractions", "productivity"),
                2,
                "Completing the whole job in 6 days means 1/6 of the job is completed per day.",
                73.0,
                List.of(
                        option("1/3", false),
                        option("1/6", true),
                        option("2/3", false),
                        option("6", false)));

        createMcqQuestion(
                PracticeQuestionType.APTITUDE_MCQ,
                "Average speed",
                "A car travels 150 km in 3 hours. What is its average speed?",
                DifficultyLevel.BEGINNER,
                "Speed Distance Time",
                List.of("speed", "distance", "time"),
                2,
                "Average speed = distance / time = 150 / 3 = 50 km/h.",
                82.0,
                List.of(
                        option("45 km/h", false),
                        option("50 km/h", true),
                        option("55 km/h", false),
                        option("60 km/h", false)));

        createMcqQuestion(
                PracticeQuestionType.APTITUDE_MCQ,
                "Probability of a coin toss",
                "What is the probability of getting heads in a fair coin toss?",
                DifficultyLevel.BEGINNER,
                "Probability",
                List.of("probability", "coin-toss", "basics"),
                2,
                "There are two equally likely outcomes and one favorable outcome, so probability is 1/2.",
                86.0,
                List.of(
                        option("1/4", false),
                        option("1/3", false),
                        option("1/2", true),
                        option("2/3", false)));

        createMcqQuestion(
                PracticeQuestionType.APTITUDE_MCQ,
                "Number pattern",
                "Find the next number in the series: 2, 6, 12, 20, 30, ?",
                DifficultyLevel.INTERMEDIATE,
                "Logical Reasoning",
                List.of("series", "patterns", "reasoning"),
                4,
                "The pattern adds consecutive even differences: +4, +6, +8, +10, so next is +12 giving 42.",
                46.0,
                List.of(
                        option("40", false),
                        option("42", true),
                        option("44", false),
                        option("48", false)));
    }

    private void createMcqQuestion(
            PracticeQuestionType type,
            String title,
            String prompt,
            DifficultyLevel difficulty,
            String topic,
            List<String> tags,
            int estimatedSolveTimeMinutes,
            String explanation,
            double successRate,
            List<OptionSeed> options) {
        PracticeQuestion question = new PracticeQuestion();
        question.setQuestionType(type);
        question.setTitle(title);
        question.setPrompt(prompt);
        question.setDifficultyLevel(difficulty);
        question.setTopic(topic);
        question.setTags(new LinkedHashSet<>(tags));
        question.setEstimatedSolveTimeMinutes(estimatedSolveTimeMinutes);
        question.setSuccessRate(BigDecimal.valueOf(successRate));
        question.setIsActive(true);

        PracticeQuestion savedQuestion = practiceQuestionRepository.save(question);

        if (type == PracticeQuestionType.PROGRAMMING_MCQ) {
            ProgrammingMCQ programmingMCQ = new ProgrammingMCQ();
            programmingMCQ.setQuestion(savedQuestion);
            programmingMCQ.setExplanation(explanation);
            programmingMCQRepository.save(programmingMCQ);
        } else if (type == PracticeQuestionType.APTITUDE_MCQ) {
            AptitudeMCQ aptitudeMCQ = new AptitudeMCQ();
            aptitudeMCQ.setQuestion(savedQuestion);
            aptitudeMCQ.setExplanation(explanation);
            aptitudeMCQRepository.save(aptitudeMCQ);
        }

        for (int index = 0; index < options.size(); index++) {
            OptionSeed option = options.get(index);
            MCQOption mcqOption = new MCQOption();
            mcqOption.setQuestion(savedQuestion);
            mcqOption.setOptionText(option.text());
            mcqOption.setDisplayOrder(index + 1);
            mcqOption.setIsCorrect(option.correct());
            mcqOptionRepository.save(mcqOption);
        }
    }

    private static OptionSeed option(String text, boolean correct) {
        return new OptionSeed(text, correct);
    }

    private record OptionSeed(String text, boolean correct) {}
}