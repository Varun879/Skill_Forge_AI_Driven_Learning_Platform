package com.skillforge.domain.course.dto;

import java.time.LocalDateTime;

import com.skillforge.domain.course.entity.CourseModule;

public class CourseModuleResponse {

    private Long id;
    private Long courseId;
    private String title;
    private String content;
    private String videoUrl;
    private Integer orderIndex;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CourseModuleResponse from(CourseModule module) {
        CourseModuleResponse response = new CourseModuleResponse();
        response.id = module.getId();
        response.courseId = module.getCourse().getId();
        response.title = module.getTitle();
        response.content = module.getContent();
        response.videoUrl = module.getVideoUrl();
        response.orderIndex = module.getOrderIndex();
        response.createdAt = module.getCreatedAt();
        response.updatedAt = module.getUpdatedAt();
        return response;
    }

    public Long getId() { return id; }
    public Long getCourseId() { return courseId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getVideoUrl() { return videoUrl; }
    public Integer getOrderIndex() { return orderIndex; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
