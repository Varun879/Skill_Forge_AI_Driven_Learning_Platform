package com.skillforge.domain.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SendChatMessageRequest {

    @NotNull(message = "roomId is required")
    private Long roomId;

    @NotBlank(message = "message is required")
    @Size(max = 2000, message = "message must be at most 2000 characters")
    private String message;

    public Long getRoomId() {
        return roomId;
    }

    public String getMessage() {
        return message;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
