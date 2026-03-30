package com.skillforge.domain.practice.dto;

import com.skillforge.common.enums.PracticeSessionType;

import jakarta.validation.constraints.NotNull;

public class PracticeSessionStartRequest {

    @NotNull(message = "sessionType is required")
    private PracticeSessionType sessionType;

    public PracticeSessionStartRequest() {
    }

    public PracticeSessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(PracticeSessionType sessionType) {
        this.sessionType = sessionType;
    }
}
