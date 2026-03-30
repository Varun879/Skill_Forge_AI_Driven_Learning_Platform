package com.skillforge.domain.courseexam.entity;

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
    name = "course_exam_questions",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_course_exam_question", columnNames = {"course_exam_id", "question_id"})
    },
    indexes = {
        @Index(name = "idx_course_exam_questions_exam", columnList = "course_exam_id"),
        @Index(name = "idx_course_exam_questions_question", columnList = "question_id")
    }
)
public class CourseExamQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_exam_id", nullable = false)
    private CourseExam courseExam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private PracticeQuestion question;

    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;

    public Long getId() { return id; }
    public CourseExam getCourseExam() { return courseExam; }
    public PracticeQuestion getQuestion() { return question; }
    public Integer getQuestionOrder() { return questionOrder; }

    public void setCourseExam(CourseExam courseExam) { this.courseExam = courseExam; }
    public void setQuestion(PracticeQuestion question) { this.question = question; }
    public void setQuestionOrder(Integer questionOrder) { this.questionOrder = questionOrder; }
}
