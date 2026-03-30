package com.skillforge.domain.courseexam.entity;

import com.skillforge.domain.practice.entity.MCQOption;
import com.skillforge.domain.practice.entity.PracticeQuestion;

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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "course_exam_attempt_answers",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_course_exam_attempt_question", columnNames = {"attempt_id", "question_id"})
    },
    indexes = {
        @Index(name = "idx_course_exam_attempt_answers_attempt", columnList = "attempt_id")
    }
)
public class CourseExamAttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private CourseExamAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private PracticeQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private MCQOption selectedOption;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    public Long getId() { return id; }
    public CourseExamAttempt getAttempt() { return attempt; }
    public PracticeQuestion getQuestion() { return question; }
    public MCQOption getSelectedOption() { return selectedOption; }
    public Boolean getIsCorrect() { return isCorrect; }

    public void setAttempt(CourseExamAttempt attempt) { this.attempt = attempt; }
    public void setQuestion(PracticeQuestion question) { this.question = question; }
    public void setSelectedOption(MCQOption selectedOption) { this.selectedOption = selectedOption; }
    public void setIsCorrect(Boolean correct) { isCorrect = correct; }
}
