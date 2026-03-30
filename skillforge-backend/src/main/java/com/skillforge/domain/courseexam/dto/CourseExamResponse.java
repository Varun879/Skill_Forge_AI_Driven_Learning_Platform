package com.skillforge.domain.courseexam.dto;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import com.skillforge.domain.courseexam.entity.CourseExam;

public class CourseExamResponse {

    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private Integer durationMinutes;
    private Boolean published;
    private Integer questionCount;
    private List<CourseExamQuestionResponse> questions;
    private LocalDateTime createdAt;

    private CourseExamResponse() {}

    public static CourseExamResponse from(CourseExam exam) {
        CourseExamResponse response = new CourseExamResponse();
        response.id = exam.getId();
        response.courseId = exam.getCourse().getId();
        response.title = exam.getTitle();
        response.description = exam.getDescription();
        response.durationMinutes = exam.getDurationMinutes();
        response.published = exam.getIsPublished();
        response.questions = exam.getQuestions()
                .stream()
                .sorted(Comparator.comparing(q -> q.getQuestionOrder() == null ? Integer.MAX_VALUE : q.getQuestionOrder()))
                .map(q -> new CourseExamQuestionResponse(
                        q.getQuestion().getId(),
                        q.getQuestion().getTitle(),
                        q.getQuestion().getTopic(),
                        q.getQuestion().getDifficultyLevel()))
                .toList();
        response.questionCount = response.questions.size();
        response.createdAt = exam.getCreatedAt();
        return response;
    }

    public Long getId() { return id; }
    public Long getCourseId() { return courseId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public Boolean getPublished() { return published; }
    public Integer getQuestionCount() { return questionCount; }
    public List<CourseExamQuestionResponse> getQuestions() { return questions; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
