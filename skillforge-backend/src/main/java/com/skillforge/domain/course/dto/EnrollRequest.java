package com.skillforge.domain.course.dto;

import jakarta.validation.constraints.NotNull;

public class EnrollRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    public Long getCourseId()            { return courseId; }
    public void setCourseId(Long id)     { this.courseId = id; }
}
