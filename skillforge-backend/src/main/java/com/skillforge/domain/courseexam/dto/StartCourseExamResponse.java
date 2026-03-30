package com.skillforge.domain.courseexam.dto;

import java.time.LocalDateTime;
import java.util.List;

public class StartCourseExamResponse {

    public record OptionItem(Long optionId, String text) {}

    public record ExamQuestionItem(
            Long questionId,
            String title,
            String prompt,
            String topic,
            String difficulty,
            Integer order,
            List<OptionItem> options
    ) {}

    private Long attemptId;
    private Long courseExamId;
    private String examTitle;
    private Integer durationMinutes;
    private LocalDateTime startedAt;
    private List<ExamQuestionItem> questions;

    public StartCourseExamResponse(Long attemptId,
                                   Long courseExamId,
                                   String examTitle,
                                   Integer durationMinutes,
                                   LocalDateTime startedAt,
                                   List<ExamQuestionItem> questions) {
        this.attemptId = attemptId;
        this.courseExamId = courseExamId;
        this.examTitle = examTitle;
        this.durationMinutes = durationMinutes;
        this.startedAt = startedAt;
        this.questions = questions;
    }

    public Long getAttemptId() { return attemptId; }
    public Long getCourseExamId() { return courseExamId; }
    public String getExamTitle() { return examTitle; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public List<ExamQuestionItem> getQuestions() { return questions; }
}
