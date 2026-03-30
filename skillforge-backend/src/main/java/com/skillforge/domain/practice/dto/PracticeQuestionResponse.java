package com.skillforge.domain.practice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.skillforge.common.enums.DifficultyLevel;
import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.domain.practice.entity.MCQOption;
import com.skillforge.domain.practice.entity.PracticeQuestion;

public class PracticeQuestionResponse {

    private Long id;
    private PracticeQuestionType questionType;
    private String title;
    private String prompt;
    private DifficultyLevel difficultyLevel;
    private String topic;
    private Set<String> tags;
    private Integer estimatedSolveTimeMinutes;
    private BigDecimal successRate;
    private List<MCQOptionResponse> options;
    private LocalDateTime createdAt;

    private PracticeQuestionResponse() {}

    public static PracticeQuestionResponse from(PracticeQuestion question) {
        PracticeQuestionResponse response = new PracticeQuestionResponse();
        response.id = question.getId();
        response.questionType = question.getQuestionType();
        response.title = question.getTitle();
        response.prompt = question.getPrompt();
        response.difficultyLevel = question.getDifficultyLevel();
        response.topic = question.getTopic();
        response.tags = question.getTags();
        response.estimatedSolveTimeMinutes = question.getEstimatedSolveTimeMinutes();
        response.successRate = question.getSuccessRate();
        response.options = Optional.ofNullable(question.getOptions())
            .orElse(List.of())
            .stream()
                .sorted(Comparator.comparing(MCQOption::getDisplayOrder))
                .map(MCQOptionResponse::from)
                .toList();
        response.createdAt = question.getCreatedAt();
        return response;
    }

    public Long getId() { return id; }
    public PracticeQuestionType getQuestionType() { return questionType; }
    public String getTitle() { return title; }
    public String getPrompt() { return prompt; }
    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public String getTopic() { return topic; }
    public Set<String> getTags() { return tags; }
    public Integer getEstimatedSolveTimeMinutes() { return estimatedSolveTimeMinutes; }
    public BigDecimal getSuccessRate() { return successRate; }
    public List<MCQOptionResponse> getOptions() { return options; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
