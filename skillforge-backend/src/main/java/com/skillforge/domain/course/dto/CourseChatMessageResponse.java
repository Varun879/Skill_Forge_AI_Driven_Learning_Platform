package com.skillforge.domain.course.dto;

import java.time.LocalDateTime;

public record CourseChatMessageResponse(
        Long id,
        Long courseId,
        Long senderId,
        String senderName,
        String senderRole,
        String message,
        LocalDateTime createdAt
) {}