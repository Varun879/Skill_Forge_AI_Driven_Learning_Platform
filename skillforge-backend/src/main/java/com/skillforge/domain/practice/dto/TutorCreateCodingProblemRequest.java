package com.skillforge.domain.practice.dto;

import java.util.List;

import com.skillforge.common.enums.DifficultyLevel;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TutorCreateCodingProblemRequest {

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

    public TutorCreateCodingProblemRequest() {
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
}
