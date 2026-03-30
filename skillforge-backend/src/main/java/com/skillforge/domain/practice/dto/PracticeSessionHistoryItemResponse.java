package com.skillforge.domain.practice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.skillforge.common.enums.PracticeSessionType;

public class PracticeSessionHistoryItemResponse {

    private Long sessionId;
    private PracticeSessionType sessionType;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer questionsAttempted;
    private Integer totalTimeSpentSeconds;
    private BigDecimal accuracyRate;
    private Integer topicsCovered;
    private List<SessionQuestionResponse> recentAttempts;

    public PracticeSessionHistoryItemResponse(
            Long sessionId,
            PracticeSessionType sessionType,
            LocalDateTime startedAt,
            LocalDateTime endedAt,
            Integer questionsAttempted,
            Integer totalTimeSpentSeconds,
            BigDecimal accuracyRate,
            Integer topicsCovered,
            List<SessionQuestionResponse> recentAttempts) {
        this.sessionId = sessionId;
        this.sessionType = sessionType;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.questionsAttempted = questionsAttempted;
        this.totalTimeSpentSeconds = totalTimeSpentSeconds;
        this.accuracyRate = accuracyRate;
        this.topicsCovered = topicsCovered;
        this.recentAttempts = recentAttempts;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public PracticeSessionType getSessionType() {
        return sessionType;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public Integer getQuestionsAttempted() {
        return questionsAttempted;
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
