package com.skillforge.domain.practice.recommendation.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Internal DTO that maps the JSON object returned by the AI model for a
 * single generated MCQ.  Fields match the snake_case keys in the AI
 * response payload; Jackson handles the mapping.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeneratedMCQDto {

    @JsonProperty("question")
    private String question;

    @JsonProperty("category")
    private String category;

    @JsonProperty("difficulty")
    private String difficulty;

    @JsonProperty("options")
    private List<String> options;

    /**
     * The letter of the correct option: "A", "B", "C", or "D".
     * Corresponds to {@code options[0..3]} respectively.
     */
    @JsonProperty("correctOption")
    private String correctOption;

    @JsonProperty("explanation")
    private String explanation;

    /**
     * Estimated solve time in <em>seconds</em> as returned by the AI.
     * Converted to minutes on persistence.
     */
    @JsonProperty("estimatedSolveTime")
    private int estimatedSolveTime;

    public GeneratedMCQDto() {}

    public String getQuestion() { return question; }
    public String getCategory() { return category; }
    public String getDifficulty() { return difficulty; }
    public List<String> getOptions() { return options; }
    public String getCorrectOption() { return correctOption; }
    public String getExplanation() { return explanation; }
    public int getEstimatedSolveTime() { return estimatedSolveTime; }

    public void setQuestion(String question) { this.question = question; }
    public void setCategory(String category) { this.category = category; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setOptions(List<String> options) { this.options = options; }
    public void setCorrectOption(String correctOption) { this.correctOption = correctOption; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public void setEstimatedSolveTime(int estimatedSolveTime) { this.estimatedSolveTime = estimatedSolveTime; }
}
