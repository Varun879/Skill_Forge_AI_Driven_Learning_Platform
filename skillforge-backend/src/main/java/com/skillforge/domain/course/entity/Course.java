package com.skillforge.domain.course.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.skillforge.common.enums.CourseStatus;
import com.skillforge.common.enums.DifficultyLevel;
import com.skillforge.domain.user.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "courses",
    indexes = {
        @Index(name = "idx_courses_tutor_id", columnList = "tutor_id"),
        @Index(name = "idx_courses_status",   columnList = "status")
    }
)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tutor_id", nullable = false)
    private User tutor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CourseStatus status = CourseStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false, length = 20)
    private DifficultyLevel difficultyLevel;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "youtube_video_url", length = 500)
    private String youtubeVideoUrl;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseTag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseEnrollment> enrollments = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Course() {}

    // ── Getters ───────────────────────────────────────────────────────────

    public Long getId()                     { return id; }
    public String getTitle()                { return title; }
    public String getDescription()          { return description; }
    public User getTutor()                  { return tutor; }
    public CourseStatus getStatus()         { return status; }
    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public BigDecimal getPrice()            { return price; }
    public String getYoutubeVideoUrl()      { return youtubeVideoUrl; }
    public List<CourseTag> getTags()        { return tags; }
    public List<CourseEnrollment> getEnrollments() { return enrollments; }
    public LocalDateTime getCreatedAt()     { return createdAt; }
    public LocalDateTime getUpdatedAt()     { return updatedAt; }

    // ── Setters ───────────────────────────────────────────────────────────

    public void setTitle(String title)                         { this.title = title; }
    public void setDescription(String description)             { this.description = description; }
    public void setTutor(User tutor)                           { this.tutor = tutor; }
    public void setStatus(CourseStatus status)                 { this.status = status; }
    public void setDifficultyLevel(DifficultyLevel level)      { this.difficultyLevel = level; }
    public void setPrice(BigDecimal price)                     { this.price = price; }
    public void setYoutubeVideoUrl(String youtubeVideoUrl)     { this.youtubeVideoUrl = youtubeVideoUrl; }
}
