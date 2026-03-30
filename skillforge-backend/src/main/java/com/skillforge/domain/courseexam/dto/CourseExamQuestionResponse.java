package com.skillforge.domain.courseexam.dto;

import com.skillforge.common.enums.DifficultyLevel;

public class CourseExamQuestionResponse {

    private Long questionId;
    private String title;
    private String topic;
    private DifficultyLevel difficultyLevel;

    public CourseExamQuestionResponse(Long questionId, String title, String topic, DifficultyLevel difficultyLevel) {
        this.questionId = questionId;
        this.title = title;
        this.topic = topic;
        this.difficultyLevel = difficultyLevel;
    }

    public Long getQuestionId() { return questionId; }
    public String getTitle() { return title; }
    public String getTopic() { return topic; }
    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
}
