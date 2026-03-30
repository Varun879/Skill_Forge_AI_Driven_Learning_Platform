package com.skillforge.domain.chat.dto;

import java.time.LocalDateTime;

import com.skillforge.domain.chat.entity.ChatMessage;

public class ChatMessageResponse {

    private Long id;
    private Long roomId;
    private Long senderId;
    private String senderRole;
    private String senderName;
    private String message;
    private LocalDateTime timestamp;

    public static ChatMessageResponse from(ChatMessage message) {
        ChatMessageResponse out = new ChatMessageResponse();
        out.id = message.getId();
        out.roomId = message.getChatRoom().getId();
        out.senderId = message.getSender().getId();
        out.senderRole = message.getSenderRole().name();
        out.senderName = message.getSender().getFirstName() + " " + message.getSender().getLastName();
        out.message = message.getMessage();
        out.timestamp = message.getMessageTime();
        return out;
    }

    public Long getId() {
        return id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
