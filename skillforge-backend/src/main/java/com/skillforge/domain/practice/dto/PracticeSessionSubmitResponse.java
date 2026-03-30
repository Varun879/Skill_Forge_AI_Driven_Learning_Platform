package com.skillforge.domain.practice.dto;

import java.math.BigDecimal;
import java.util.List;

public class PracticeSessionSubmitResponse {

    private Long sessionId;
    private Long questionId;
    private Boolean isCorrect;
    private Integer totalQuestionsAttempted;
    private Integer totalTimeSpentSeconds;
    private BigDecimal accuracyRate;
    private Integer topicsCovered;
    private List<SessionQuestionResponse> recentAttempts;

    public PracticeSessionSubmitResponse(
            Long sessionId,
            Long questionId,
            Boolean isCorrect,
            Integer totalQuestionsAttempted,
            Integer totalTimeSpentSeconds,
            BigDecimal accuracyRate,
            Integer topicsCovered,
            List<SessionQuestionResponse> recentAttempts) {
        this.sessionId = sessionId;
        this.questionId = questionId;
        this.isCorrect = isCorrect;
        this.totalQuestionsAttempted = totalQuestionsAttempted;
        this.totalTimeSpentSeconds = totalTimeSpentSeconds;
        this.accuracyRate = accuracyRate;
        this.topicsCovered = topicsCovered;
        this.recentAttempts = recentAttempts;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public Integer getTotalQuestionsAttempted() {
        return totalQuestionsAttempted;
    }

    public Integer getTotalTimeSpentSeconds() {
        return totalTimeSpentSeconds;
    }

    public BigDecimal getAccuracyRate() {
        return accuracyRate;
    }

    public Integer getTopicsCovered() {
        return topicsCovered;
    }

    public List<SessionQuestionResponse> getRecentAttempts() {
        return recentAttempts;
    }
}
