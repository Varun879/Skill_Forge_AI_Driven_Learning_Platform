package com.skillforge.domain.problem.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.skillforge.common.enums.DifficultyLevel;
import com.skillforge.domain.problem.entity.Problem;

public class ProblemListResponse {

    private Long id;
    private String title;
    private DifficultyLevel difficultyLevel;
    private List<String> tags;
    private int testCaseCount;
    private LocalDateTime createdAt;

    private ProblemListResponse() {}

    public static ProblemListResponse from(Problem problem) {
        ProblemListResponse response = new ProblemListResponse();
        response.id = problem.getId();
        response.title = problem.getTitle();
        response.difficultyLevel = problem.getDifficultyLevel();
        response.tags = problem.getTags().stream().map(tag -> tag.getName()).toList();
        response.testCaseCount = problem.getTestCases().size();
        response.createdAt = problem.getCreatedAt();
        return response;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public List<String> getTags() { return tags; }
    public int getTestCaseCount() { return testCaseCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
