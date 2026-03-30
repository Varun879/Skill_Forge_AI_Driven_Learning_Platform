package com.skillforge.domain.practice.recommendation.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.domain.practice.dto.PracticeQuestionResponse;
import com.skillforge.domain.practice.entity.PracticeQuestion;
import com.skillforge.domain.practice.recommendation.dto.MCQCategoryQuestionsResponse;
import com.skillforge.domain.practice.repository.PracticeQuestionRepository;

@Service
public class MCQCategoryQueryService {

    @Value("${practice.ai.require-real-questions:true}")
    private boolean requireRealQuestions;

    private final CategoryAvailabilityService availabilityService;
    private final UserPerformanceAnalyzer performanceAnalyzer;
    private final PracticeQuestionRepository questionRepository;

    public MCQCategoryQueryService(
            CategoryAvailabilityService availabilityService,
            UserPerformanceAnalyzer performanceAnalyzer,
            PracticeQuestionRepository questionRepository) {
        this.availabilityService = availabilityService;
        this.performanceAnalyzer = performanceAnalyzer;
        this.questionRepository = questionRepository;
    }

    @Transactional
    public List<MCQCategoryQuestionsResponse> getCategoryQuestions(PracticeQuestionType questionType) {
        if (questionType != PracticeQuestionType.PROGRAMMING_MCQ
                && questionType != PracticeQuestionType.APTITUDE_MCQ) {
            throw new IllegalArgumentException("questionType must be PROGRAMMING_MCQ or APTITUDE_MCQ");
        }

        availabilityService.ensureCategoriesPopulated(questionType);

        List<PracticeQuestion> questions = questionRepository.findFiltered(questionType, null, null);
        List<PracticeQuestion> sortedQuestions = questions.stream()
                .sorted((a, b) -> {
                    boolean aDemo = isDemoQuestion(a);
                    boolean bDemo = isDemoQuestion(b);
                    if (aDemo == bDemo) {
                        return 0;
                    }
                    return aDemo ? 1 : -1;
                })
                .toList();

        Map<String, List<PracticeQuestionResponse>> grouped = new LinkedHashMap<>();
        Map<String, Integer> categoryCount = new HashMap<>();
        for (String category : performanceAnalyzer.allCategoriesForType(questionType)) {
            grouped.put(category, new ArrayList<>());
            categoryCount.put(category.toLowerCase(), 0);
        }

        for (PracticeQuestion question : sortedQuestions) {
            if (requireRealQuestions && isDemoQuestion(question)) {
                continue;
            }

            String topic = question.getTopic();
            String key = topic.toLowerCase();

            grouped.computeIfAbsent(topic, ignored -> new ArrayList<>());
            categoryCount.putIfAbsent(key, 0);

            if (categoryCount.get(key) < 10) {
                grouped.get(topic).add(PracticeQuestionResponse.from(question));
                categoryCount.put(key, categoryCount.get(key) + 1);
            }
        }

        List<MCQCategoryQuestionsResponse> result = new ArrayList<>();
        for (Map.Entry<String, List<PracticeQuestionResponse>> entry : grouped.entrySet()) {
            result.add(new MCQCategoryQuestionsResponse(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    private boolean isDemoQuestion(PracticeQuestion question) {
        return question.getTags() != null
                && question.getTags().stream().anyMatch(tag -> "auto-generated".equalsIgnoreCase(tag));
    }
}
