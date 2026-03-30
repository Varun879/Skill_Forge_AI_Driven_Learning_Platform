package com.skillforge.domain.chat.dto;

import java.time.LocalDateTime;

import com.skillforge.domain.chat.entity.ChatRoom;

public class ChatRoomResponse {

    private Long id;
    private Long courseId;
    private String courseTitle;
    private Long tutorId;
    private String tutorName;
    private Long studentId;
    private String studentName;
    private LocalDateTime createdAt;

    public static ChatRoomResponse from(ChatRoom room) {
        ChatRoomResponse out = new ChatRoomResponse();
        out.id = room.getId();
        out.courseId = room.getCourse().getId();
        out.courseTitle = room.getCourse().getTitle();
        out.tutorId = room.getTutor().getId();
        out.tutorName = room.getTutor().getFirstName() + " " + room.getTutor().getLastName();
        out.studentId = room.getStudent().getId();
        out.studentName = room.getStudent().getFirstName() + " " + room.getStudent().getLastName();
        out.createdAt = room.getCreatedAt();
        return out;
    }

    public Long getId() {
        return id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public Long getTutorId() {
        return tutorId;
    }

    public String getTutorName() {
        return tutorName;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
