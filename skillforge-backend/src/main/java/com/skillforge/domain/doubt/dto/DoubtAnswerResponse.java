package com.skillforge.domain.doubt.dto;

import java.time.LocalDateTime;

import com.skillforge.domain.doubt.entity.DoubtAnswer;

public class DoubtAnswerResponse {

    private final Long id;
    private final Long doubtId;
    private final Long tutorId;
    private final String answer;
    private final LocalDateTime createdAt;
    private final String tutorName;

    public DoubtAnswerResponse(Long id,
                               Long doubtId,
                               Long tutorId,
                               String answer,
                               LocalDateTime createdAt,
                               String tutorName) {
        this.id = id;
        this.doubtId = doubtId;
        this.tutorId = tutorId;
        this.answer = answer;
        this.createdAt = createdAt;
        this.tutorName = tutorName;
    }

    public static DoubtAnswerResponse from(DoubtAnswer answer) {
        String firstName = answer.getTutor().getFirstName();
        String lastName = answer.getTutor().getLastName();
        String fullName = ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
        String tutorName = fullName.isEmpty() ? answer.getTutor().getUsername() : fullName;

        return new DoubtAnswerResponse(
                answer.getId(),
                answer.getDoubt().getId(),
                answer.getTutor().getId(),
                answer.getAnswer(),
                answer.getCreatedAt(),
                tutorName);
    }

    public Long getId() { return id; }
    public Long getDoubtId() { return doubtId; }
    public Long getTutorId() { return tutorId; }
    public String getAnswer() { return answer; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getTutorName() { return tutorName; }
}