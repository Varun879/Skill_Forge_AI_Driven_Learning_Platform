package com.skillforge.domain.problem.dto;

import java.util.List;

import com.skillforge.common.enums.DifficultyLevel;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

public class UpdateProblemRequest {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String description;

    private DifficultyLevel difficultyLevel;

    private List<String> constraints;

    private List<String> tags;

    @Valid
    private List<ProblemTestCaseRequest> testCases;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public List<String> getConstraints() { return constraints; }
    public void setConstraints(List<String> constraints) { this.constraints = constraints; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public List<ProblemTestCaseRequest> getTestCases() { return testCases; }
    public void setTestCases(List<ProblemTestCaseRequest> testCases) { this.testCases = testCases; }
}
