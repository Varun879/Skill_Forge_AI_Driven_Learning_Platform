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
    name = "problem_tags",
    indexes = {
        @Index(name = "idx_problem_tags_problem_id", columnList = "problem_id")
    }
)
public class ProblemTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false, length = 50)
    private String name;

    public ProblemTag() {}

    public ProblemTag(Problem problem, String name) {
        this.problem = problem;
        this.name = name;
    }

    public Long getId() { return id; }
    public Problem getProblem() { return problem; }
    public String getName() { return name; }

    public void setProblem(Problem problem) { this.problem = problem; }
    public void setName(String name) { this.name = name; }
}
