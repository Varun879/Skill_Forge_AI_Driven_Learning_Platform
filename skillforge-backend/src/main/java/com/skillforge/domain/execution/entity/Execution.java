package com.skillforge.domain.execution.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.skillforge.domain.problem.entity.Problem;
import com.skillforge.domain.submission.entity.Submission;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "executions", indexes = {
        @Index(name = "idx_executions_learner_id", columnList = "learner_id"),
        @Index(name = "idx_executions_problem_id", columnList = "problem_id"),
        @Index(name = "idx_executions_status", columnList = "status"),
        @Index(name = "idx_executions_created_at", columnList = "created_at")
})
public class Execution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learner_id", nullable = false)
    private User learner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private Submission submission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExecutionMode mode;

    @Column(nullable = false, length = 20)
    private String language;

    @Column(name = "source_code", nullable = false, columnDefinition = "TEXT")
    private String sourceCode;

    @Column(name = "stdin_data", columnDefinition = "TEXT")
    private String stdinData;

    @Column(name = "stdout_data", columnDefinition = "TEXT")
    private String stdoutData;

    @Column(name = "stderr_data", columnDefinition = "TEXT")
    private String stderrData;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ExecutionStatus status;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "memory_usage_kb")
    private Long memoryUsageKb;

    @Column(name = "cache_hit", nullable = false)
    private boolean cacheHit = false;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "execution", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExecutionTestResult> testResults = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public User getLearner() { return learner; }
    public Problem getProblem() { return problem; }
    public Submission getSubmission() { return submission; }
    public ExecutionMode getMode() { return mode; }
    public String getLanguage() { return language; }
    public String getSourceCode() { return sourceCode; }
    public String getStdinData() { return stdinData; }
    public String getStdoutData() { return stdoutData; }
    public String getStderrData() { return stderrData; }
    public ExecutionStatus getStatus() { return status; }
    public Long getExecutionTimeMs() { return executionTimeMs; }
    public Long getMemoryUsageKb() { return memoryUsageKb; }
    public boolean isCacheHit() { return cacheHit; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public List<ExecutionTestResult> getTestResults() { return testResults; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setLearner(User learner) { this.learner = learner; }
    public void setProblem(Problem problem) { this.problem = problem; }
    public void setSubmission(Submission submission) { this.submission = submission; }
    public void setMode(ExecutionMode mode) { this.mode = mode; }
    public void setLanguage(String language) { this.language = language; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
    public void setStdinData(String stdinData) { this.stdinData = stdinData; }
    public void setStdoutData(String stdoutData) { this.stdoutData = stdoutData; }
    public void setStderrData(String stderrData) { this.stderrData = stderrData; }
    public void setStatus(ExecutionStatus status) { this.status = status; }
    public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    public void setMemoryUsageKb(Long memoryUsageKb) { this.memoryUsageKb = memoryUsageKb; }
    public void setCacheHit(boolean cacheHit) { this.cacheHit = cacheHit; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
