package com.skillforge.domain.course.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CourseModuleRequest {

    @NotBlank(message = "Module title is required")
    @Size(max = 255, message = "Module title must not exceed 255 characters")
    private String title;

    private String content;

    @JsonAlias("video_url")
    @Size(max = 500, message = "Video URL must not exceed 500 characters")
    private String videoUrl;

    @JsonAlias("order_index")
    private Integer orderIndex;

    @JsonAlias("notes")
    public void setNotes(String notes) {
        if (this.content == null || this.content.isBlank()) {
            this.content = notes;
        }
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
}
