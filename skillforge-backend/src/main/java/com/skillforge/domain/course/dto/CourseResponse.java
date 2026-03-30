package com.skillforge.domain.course.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.skillforge.common.enums.CourseStatus;
import com.skillforge.common.enums.DifficultyLevel;
import com.skillforge.domain.course.entity.Course;

public class CourseResponse {

    private Long id;
    private String title;
    private String description;
    private Long tutorId;
    private String tutorName;
    private CourseStatus status;
    private DifficultyLevel difficultyLevel;
    private BigDecimal price;
    private String youtubeVideoUrl;
    private List<String> tags;
    private int enrollmentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private CourseResponse() {}

    public static CourseResponse from(Course course) {
        CourseResponse r = new CourseResponse();
        r.id              = course.getId();
        r.title           = course.getTitle();
        r.description     = course.getDescription();
        r.tutorId         = course.getTutor().getId();
        r.tutorName       = course.getTutor().getFirstName() + " " + course.getTutor().getLastName();
        r.status          = course.getStatus();
        r.difficultyLevel = course.getDifficultyLevel();
        r.price           = course.getPrice();
        r.youtubeVideoUrl = course.getYoutubeVideoUrl();
        r.tags            = course.getTags().stream()
                                   .map(t -> t.getName())
                                   .toList();
        r.enrollmentCount = course.getEnrollments().size();
        r.createdAt       = course.getCreatedAt();
        r.updatedAt       = course.getUpdatedAt();
        return r;
    }

    // ── Getters ───────────────────────────────────────────────────────────

    public Long getId()                     { return id; }
    public String getTitle()                { return title; }
    public String getDescription()          { return description; }
    public Long getTutorId()                { return tutorId; }
    public String getTutorName()            { return tutorName; }
    public CourseStatus getStatus()         { return status; }
    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public BigDecimal getPrice()            { return price; }
    public String getYoutubeVideoUrl()      { return youtubeVideoUrl; }
    public List<String> getTags()           { return tags; }
    public int getEnrollmentCount()         { return enrollmentCount; }
    public LocalDateTime getCreatedAt()     { return createdAt; }
    public LocalDateTime getUpdatedAt()     { return updatedAt; }
}
