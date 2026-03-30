package com.skillforge.domain.practice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.skillforge.common.enums.DifficultyLevel;
import com.skillforge.common.enums.PracticeQuestionType;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "practice_questions",
    indexes = {
        @Index(name = "idx_practice_questions_type_difficulty_topic", columnList = "question_type,difficulty_level,topic"),
        @Index(name = "idx_practice_questions_topic_success_rate", columnList = "topic,success_rate"),
        @Index(name = "idx_practice_questions_is_active_created_at", columnList = "is_active,created_at")
    }
)
public class PracticeQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 30)
    private PracticeQuestionType questionType;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "prompt", nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false, length = 20)
    private DifficultyLevel difficultyLevel;

    @Column(nullable = false, length = 120)
    private String topic;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "practice_question_tags", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "tag", nullable = false, length = 50)
    private Set<String> tags = new LinkedHashSet<>();

    @Column(name = "estimated_solve_time_minutes", nullable = false)
    private Integer estimatedSolveTimeMinutes;

    @Column(name = "success_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal successRate = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToOne(mappedBy = "question", fetch = FetchType.LAZY)
    private ProgrammingMCQ programmingMCQ;

    @OneToOne(mappedBy = "question", fetch = FetchType.LAZY)
    private AptitudeMCQ aptitudeMCQ;

    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY)
    private List<MCQOption> options = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PracticeQuestion() {}

    public Long getId() { return id; }
    public PracticeQuestionType getQuestionType() { return questionType; }
    public String getTitle() { return title; }
    public String getPrompt() { return prompt; }
    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public String getTopic() { return topic; }
    public Set<String> getTags() { return tags; }
    public Integer getEstimatedSolveTimeMinutes() { return estimatedSolveTimeMinutes; }
    public BigDecimal getSuccessRate() { return successRate; }
    public Boolean getIsActive() { return isActive; }
    public ProgrammingMCQ getProgrammingMCQ() { return programmingMCQ; }
    public AptitudeMCQ getAptitudeMCQ() { return aptitudeMCQ; }
    public List<MCQOption> getOptions() { return options; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setQuestionType(PracticeQuestionType questionType) { this.questionType = questionType; }
    public void setTitle(String title) { this.title = title; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public void setTopic(String topic) { this.topic = topic; }
    public void setTags(Set<String> tags) { this.tags = tags; }
    public void setEstimatedSolveTimeMinutes(Integer estimatedSolveTimeMinutes) { this.estimatedSolveTimeMinutes = estimatedSolveTimeMinutes; }
    public void setSuccessRate(BigDecimal successRate) { this.successRate = successRate; }
    public void setIsActive(Boolean active) { isActive = active; }
}
