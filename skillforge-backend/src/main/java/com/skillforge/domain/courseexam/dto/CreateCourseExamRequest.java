package com.skillforge.domain.courseexam.dto;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateCourseExamRequest {

    @NotBlank(message = "Exam title is required")
    @Size(max = 255, message = "Exam title must not exceed 255 characters")
    private String title;

    private String description;

    @NotNull(message = "Duration in minutes is required")
    @Min(value = 5, message = "Exam duration must be at least 5 minutes")
    @Max(value = 300, message = "Exam duration must not exceed 300 minutes")
    private Integer durationMinutes;

    @NotEmpty(message = "At least one question is required")
    @Size(min = 3, max = 100, message = "Exam must contain between 3 and 100 questions")
    private List<Long> questionIds;

    private Boolean published = false;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public List<Long> getQuestionIds() { return questionIds; }
    public void setQuestionIds(List<Long> questionIds) { this.questionIds = questionIds; }

    public Boolean getPublished() { return published; }
    public void setPublished(Boolean published) { this.published = published; }
}
