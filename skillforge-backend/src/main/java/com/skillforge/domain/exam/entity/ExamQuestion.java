package com.skillforge.domain.exam.entity;

import java.time.LocalDateTime;

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
    name = "exam_questions",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_exam_question_session_question", columnNames = {"exam_session_id", "question_id"})
    },
    indexes = {
        @Index(name = "idx_exam_questions_session_id", columnList = "exam_session_id"),
        @Index(name = "idx_exam_questions_question_id", columnList = "question_id")
    }
)
public class ExamQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_session_id", nullable = false)
    private ExamSession examSession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private PracticeQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private MCQOption selectedOption;

    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    public Long getId() {
        return id;
    }

    public ExamSession getExamSession() {
        return examSession;
    }

    public PracticeQuestion getQuestion() {
        return question;
    }

    public MCQOption getSelectedOption() {
        return selectedOption;
    }

    public Integer getQuestionOrder() {
        return questionOrder;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void setExamSession(ExamSession examSession) {
        this.examSession = examSession;
    }

    public void setQuestion(PracticeQuestion question) {
        this.question = question;
    }

    public void setSelectedOption(MCQOption selectedOption) {
        this.selectedOption = selectedOption;
    }

    public void setQuestionOrder(Integer questionOrder) {
        this.questionOrder = questionOrder;
    }

    public void setIsCorrect(Boolean correct) {
        isCorrect = correct;
    }

    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }
}
