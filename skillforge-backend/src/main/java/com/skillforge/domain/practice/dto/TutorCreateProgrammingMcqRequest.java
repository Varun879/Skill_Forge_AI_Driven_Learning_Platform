package com.skillforge.domain.practice.dto;

import java.util.List;

import com.skillforge.common.enums.DifficultyLevel;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TutorCreateProgrammingMcqRequest {

    private String title;

    @NotBlank(message = "question is required")
    private String question;

    @NotNull(message = "difficultyLevel is required")
    private DifficultyLevel difficultyLevel;

    @NotBlank(message = "topic is required")
    private String topic;

    private List<@NotBlank(message = "tag cannot be blank") String> tags;

    @NotNull(message = "estimatedSolveTimeMinutes is required")
    @Min(value = 1, message = "estimatedSolveTimeMinutes must be at least 1")
    private Integer estimatedSolveTimeMinutes;

    @NotBlank(message = "explanation is required")
    private String explanation;

    @NotNull(message = "options are required")
    @Size(min = 4, max = 4, message = "exactly 4 options are required")
    @Valid
    private List<TutorMcqOptionCreateRequest> options;

    @NotNull(message = "correctOptionIndex is required")
    @Min(value = 1, message = "correctOptionIndex must be between 1 and 4")
    @Max(value = 4, message = "correctOptionIndex must be between 1 and 4")
    private Integer correctOptionIndex;

    public TutorCreateProgrammingMcqRequest() {
    }

    public String getTitle() {
        return title;
    }

    public String getQuestion() {
        return question;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public String getTopic() {
        return topic;
    }

    public List<String> getTags() {
        return tags;
    }

    public Integer getEstimatedSolveTimeMinutes() {
        return estimatedSolveTimeMinutes;
    }

    public String getExplanation() {
        return explanation;
    }

    public List<TutorMcqOptionCreateRequest> getOptions() {
        return options;
    }

    public Integer getCorrectOptionIndex() {
        return correctOptionIndex;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setEstimatedSolveTimeMinutes(Integer estimatedSolveTimeMinutes) {
        this.estimatedSolveTimeMinutes = estimatedSolveTimeMinutes;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public void setOptions(List<TutorMcqOptionCreateRequest> options) {
        this.options = options;
    }

    public void setCorrectOptionIndex(Integer correctOptionIndex) {
        this.correctOptionIndex = correctOptionIndex;
    }
}
