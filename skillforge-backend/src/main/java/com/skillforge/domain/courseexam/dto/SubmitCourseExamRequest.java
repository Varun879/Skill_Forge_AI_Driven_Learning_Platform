package com.skillforge.domain.courseexam.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class SubmitCourseExamRequest {

    public static class AnswerItem {
        @NotNull(message = "questionId is required")
        private Long questionId;

        @NotNull(message = "selectedOptionId is required")
        private Long selectedOptionId;

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }

        public Long getSelectedOptionId() { return selectedOptionId; }
        public void setSelectedOptionId(Long selectedOptionId) { this.selectedOptionId = selectedOptionId; }
    }

    @NotNull(message = "attemptId is required")
    private Long attemptId;

    @Valid
    @NotEmpty(message = "answers are required")
    private List<AnswerItem> answers;

    public Long getAttemptId() { return attemptId; }
    public void setAttemptId(Long attemptId) { this.attemptId = attemptId; }

    public List<AnswerItem> getAnswers() { return answers; }
    public void setAnswers(List<AnswerItem> answers) { this.answers = answers; }
}
