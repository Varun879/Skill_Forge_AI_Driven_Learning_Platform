package com.skillforge.domain.practice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "aptitude_mcqs")
public class AptitudeMCQ {

    @Id
    @Column(name = "question_id")
    private Long questionId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "question_id", nullable = false)
    private PracticeQuestion question;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    public AptitudeMCQ() {}

    public Long getQuestionId() { return questionId; }
    public PracticeQuestion getQuestion() { return question; }
    public String getExplanation() { return explanation; }

    public void setQuestion(PracticeQuestion question) { this.question = question; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
