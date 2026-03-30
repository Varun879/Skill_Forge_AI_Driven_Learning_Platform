package com.skillforge.domain.execution.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.skillforge.domain.problem.entity.TestCase;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "execution_test_results")
public class ExecutionTestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "execution_id", nullable = false)
    private Execution execution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_case_id")
    private TestCase testCase;

    @Column(nullable = false)
    private boolean sample;

    @Column(nullable = false)
    private boolean passed;

    @Column(nullable = false, length = 40)
    private String status;

    @Column(name = "expected_output", columnDefinition = "TEXT")
    private String expectedOutput;

    @Column(name = "actual_output", columnDefinition = "TEXT")
    private String actualOutput;

    @Column(name = "stdout_data", columnDefinition = "TEXT")
    private String stdoutData;

    @Column(name = "stderr_data", columnDefinition = "TEXT")
    private String stderrData;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "memory_usage_kb")
    private Long memoryUsageKb;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public Execution getExecution() { return execution; }
    public TestCase getTestCase() { return testCase; }
    public boolean isSample() { return sample; }
    public boolean isPassed() { return passed; }
    public String getStatus() { return status; }
    public String getExpectedOutput() { return expectedOutput; }
    public String getActualOutput() { return actualOutput; }
    public String getStdoutData() { return stdoutData; }
    public String getStderrData() { return stderrData; }
    public Long getExecutionTimeMs() { return executionTimeMs; }
    public Long getMemoryUsageKb() { return memoryUsageKb; }

    public void setExecution(Execution execution) { this.execution = execution; }
    public void setTestCase(TestCase testCase) { this.testCase = testCase; }
    public void setSample(boolean sample) { this.sample = sample; }
    public void setPassed(boolean passed) { this.passed = passed; }
    public void setStatus(String status) { this.status = status; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }
    public void setActualOutput(String actualOutput) { this.actualOutput = actualOutput; }
    public void setStdoutData(String stdoutData) { this.stdoutData = stdoutData; }
    public void setStderrData(String stderrData) { this.stderrData = stderrData; }
    public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    public void setMemoryUsageKb(Long memoryUsageKb) { this.memoryUsageKb = memoryUsageKb; }
}
