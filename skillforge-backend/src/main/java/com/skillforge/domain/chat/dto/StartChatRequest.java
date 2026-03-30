package com.skillforge.domain.chat.dto;

import jakarta.validation.constraints.NotNull;

public class StartChatRequest {

    @NotNull(message = "courseId is required")
    private Long courseId;

    private Long studentId;

    public Long getCourseId() {
        return courseId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
}
