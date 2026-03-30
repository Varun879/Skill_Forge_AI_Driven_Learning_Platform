package com.skillforge.domain.practice.dto;

import java.time.LocalDateTime;

import com.skillforge.common.enums.PracticeSessionType;
import com.skillforge.domain.practice.entity.PracticeSession;

public class PracticeSessionStartResponse {

    private Long sessionId;
    private PracticeSessionType sessionType;
    private LocalDateTime startedAt;

    private PracticeSessionStartResponse() {
    }

    public static PracticeSessionStartResponse from(PracticeSession session) {
        PracticeSessionStartResponse response = new PracticeSessionStartResponse();
        response.sessionId = session.getId();
        response.sessionType = session.getSessionType();
        response.startedAt = session.getStartedAt();
        return response;
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
}
