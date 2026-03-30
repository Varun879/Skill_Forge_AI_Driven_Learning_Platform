package com.skillforge.domain.problem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "test_cases",
    indexes = {
        @Index(name = "idx_test_cases_problem_id", columnList = "problem_id")
    }
)
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(name = "input_data", nullable = false, columnDefinition = "TEXT")
    private String inputData;

    @Column(name = "expected_output", nullable = false, columnDefinition = "TEXT")
    private String expectedOutput;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "is_sample", nullable = false)
    private boolean sample = true;

    public TestCase() {}

    public TestCase(Problem problem, String inputData, String expectedOutput, String explanation, boolean sample) {
        this.problem = problem;
        this.inputData = inputData;
        this.expectedOutput = expectedOutput;
        this.explanation = explanation;
        this.sample = sample;
    }

    public Long getId() { return id; }
    public Problem getProblem() { return problem; }
    public String getInputData() { return inputData; }
    public String getExpectedOutput() { return expectedOutput; }
    public String getExplanation() { return explanation; }
    public boolean isSample() { return sample; }

    public void setProblem(Problem problem) { this.problem = problem; }
    public void setInputData(String inputData) { this.inputData = inputData; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public void setSample(boolean sample) { this.sample = sample; }
}
