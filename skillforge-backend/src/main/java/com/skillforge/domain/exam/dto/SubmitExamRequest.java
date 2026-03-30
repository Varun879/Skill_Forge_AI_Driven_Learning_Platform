package com.skillforge.domain.exam.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class SubmitExamRequest {

    @NotNull(message = "examSessionId is required")
    private Long examSessionId;

    @Valid
    private List<AnswerItem> answers;

    public Long getExamSessionId() {
        return examSessionId;
    }

    public List<AnswerItem> getAnswers() {
        return answers;
    }

    public void setExamSessionId(Long examSessionId) {
        this.examSessionId = examSessionId;
    }

    public void setAnswers(List<AnswerItem> answers) {
        this.answers = answers;
    }

    public static class AnswerItem {

        @NotNull(message = "questionId is required")
        private Long questionId;

        @NotNull(message = "selectedOptionId is required")
        private Long selectedOptionId;

        public Long getQuestionId() {
            return questionId;
        }

        public Long getSelectedOptionId() {
            return selectedOptionId;
        }

        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }

        public void setSelectedOptionId(Long selectedOptionId) {
            this.selectedOptionId = selectedOptionId;
        }
    }
}
