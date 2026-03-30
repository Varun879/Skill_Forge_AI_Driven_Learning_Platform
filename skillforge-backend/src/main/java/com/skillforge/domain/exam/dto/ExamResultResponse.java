package com.skillforge.domain.exam.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ExamResultResponse {

    private Long examSessionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime submittedAt;
    private String status;
    private BigDecimal score;
    private long totalQuestions;
    private long correctAnswers;
    private List<ResultQuestionItem> answers;

    public ExamResultResponse(Long examSessionId,
                              LocalDateTime startTime,
                              LocalDateTime endTime,
                              LocalDateTime submittedAt,
                              String status,
                              BigDecimal score,
                              long totalQuestions,
                              long correctAnswers,
                              List<ResultQuestionItem> answers) {
        this.examSessionId = examSessionId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.submittedAt = submittedAt;
        this.status = status;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.answers = answers;
    }

    public Long getExamSessionId() {
        return examSessionId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getScore() {
        return score;
    }

    public long getTotalQuestions() {
        return totalQuestions;
    }

    public long getCorrectAnswers() {
        return correctAnswers;
    }

    public List<ResultQuestionItem> getAnswers() {
        return answers;
    }

    public static class ResultQuestionItem {
        private Long questionId;
        private Long selectedOptionId;
        private Boolean isCorrect;

        public ResultQuestionItem(Long questionId, Long selectedOptionId, Boolean isCorrect) {
            this.questionId = questionId;
            this.selectedOptionId = selectedOptionId;
            this.isCorrect = isCorrect;
        }

        public Long getQuestionId() {
            return questionId;
        }

        public Long getSelectedOptionId() {
            return selectedOptionId;
        }

        public Boolean getIsCorrect() {
            return isCorrect;
        }
    }
}
