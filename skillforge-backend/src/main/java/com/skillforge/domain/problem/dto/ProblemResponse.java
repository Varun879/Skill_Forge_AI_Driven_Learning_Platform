package com.skillforge.domain.problem.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.util.StringUtils;

import com.skillforge.common.enums.DifficultyLevel;
import com.skillforge.domain.problem.entity.Problem;

public class ProblemResponse {

    private Long id;
    private String title;
    private String description;
    private DifficultyLevel difficultyLevel;
    private List<String> constraints;
    private List<String> tags;
    private List<ProblemTestCaseResponse> testCases;
    private Long tutorId;
    private String tutorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private ProblemResponse() {}

    public static ProblemResponse from(Problem problem) {
        ProblemResponse response = new ProblemResponse();
        response.id = problem.getId();
        response.title = problem.getTitle();
        response.description = problem.getDescription();
        response.difficultyLevel = problem.getDifficultyLevel();
        response.constraints = parseConstraints(problem.getConstraintsText());
        response.tags = problem.getTags().stream().map(tag -> tag.getName()).toList();
        response.testCases = problem.getTestCases().stream().map(ProblemTestCaseResponse::from).toList();
        response.tutorId = problem.getTutor().getId();
        response.tutorName = problem.getTutor().getFirstName() + " " + problem.getTutor().getLastName();
        response.createdAt = problem.getCreatedAt();
        response.updatedAt = problem.getUpdatedAt();
        return response;
    }

    private static List<String> parseConstraints(String constraintsText) {
        if (!StringUtils.hasText(constraintsText)) {
            return Collections.emptyList();
        }

        return constraintsText.lines()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public List<String> getConstraints() { return constraints; }
    public List<String> getTags() { return tags; }
    public List<ProblemTestCaseResponse> getTestCases() { return testCases; }
    public Long getTutorId() { return tutorId; }
    public String getTutorName() { return tutorName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
