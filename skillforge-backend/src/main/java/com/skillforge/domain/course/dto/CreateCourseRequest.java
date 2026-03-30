package com.skillforge.domain.course.dto;

import java.math.BigDecimal;
import java.util.List;

import com.skillforge.common.enums.CourseStatus;
import com.skillforge.common.enums.DifficultyLevel;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateCourseRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String description;

    @NotNull(message = "Difficulty level is required")
    private DifficultyLevel difficultyLevel;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price must be non-negative")
    @Digits(integer = 8, fraction = 2, message = "Price format is invalid")
    private BigDecimal price;

    @Pattern(
        regexp = "^(https?://)?(www\\.)?(youtube\\.com|youtu\\.be)/.+$",
        message = "YouTube video URL must be a valid youtube.com or youtu.be link"
    )
    private String youtubeVideoUrl;

    private List<String> tags;

    /** If omitted, DRAFT is used. Tutors may pass PUBLISHED to publish immediately. */
    private CourseStatus status = CourseStatus.DRAFT;

    // ── Getters/Setters ───────────────────────────────────────────────────

    public String getTitle()                    { return title; }
    public void setTitle(String title)          { this.title = title; }

    public String getDescription()              { return description; }
    public void setDescription(String d)        { this.description = d; }

    public DifficultyLevel getDifficultyLevel()              { return difficultyLevel; }
    public void setDifficultyLevel(DifficultyLevel level)    { this.difficultyLevel = level; }

    public BigDecimal getPrice()                { return price; }
    public void setPrice(BigDecimal price)      { this.price = price; }

    public String getYoutubeVideoUrl()          { return youtubeVideoUrl; }
    public void setYoutubeVideoUrl(String youtubeVideoUrl) { this.youtubeVideoUrl = youtubeVideoUrl; }

    public List<String> getTags()               { return tags; }
    public void setTags(List<String> tags)      { this.tags = tags; }

    public CourseStatus getStatus()             { return status; }
    public void setStatus(CourseStatus status)  { this.status = status; }
}
