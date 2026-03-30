package com.skillforge.domain.chat.dto;

import java.time.LocalDateTime;

import com.skillforge.domain.chat.entity.CourseGroupMessage;

public class GroupChatMessageResponse {

    private Long id;
    private Long courseId;
    private Long senderId;
    private String senderRole;
    private String senderName;
    private String message;
    private LocalDateTime timestamp;

    public static GroupChatMessageResponse from(CourseGroupMessage message) {
        GroupChatMessageResponse out = new GroupChatMessageResponse();
        out.id = message.getId();
        out.courseId = message.getCourse().getId();
        out.senderId = message.getSender().getId();
        out.senderRole = message.getSenderRole().name();
        String firstName = message.getSender().getFirstName() == null ? "" : message.getSender().getFirstName().trim();
        String lastName = message.getSender().getLastName() == null ? "" : message.getSender().getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();
        out.senderName = fullName.isEmpty() ? message.getSender().getUsername() : fullName;
        out.message = message.getMessage();
        out.timestamp = message.getMessageTime();
        return out;
    }

    public Long getId() { return id; }
    public Long getCourseId() { return courseId; }
    public Long getSenderId() { return senderId; }
    public String getSenderRole() { return senderRole; }
    public String getSenderName() { return senderName; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
