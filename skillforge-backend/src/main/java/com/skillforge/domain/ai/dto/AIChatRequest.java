package com.skillforge.domain.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AIChatRequest {

    private Long userId;

    @NotBlank(message = "message is required")
    @Size(max = 2000, message = "message must be at most 2000 characters")
    private String message;

    private Long questionId;

    public Long getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }
}
