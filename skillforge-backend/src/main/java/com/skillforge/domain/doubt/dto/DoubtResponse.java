package com.skillforge.domain.doubt.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.skillforge.domain.doubt.entity.Doubt;
import com.skillforge.domain.doubt.entity.DoubtAnswer;

public class DoubtResponse {

    private final Long id;
    private final Long userId;
    private final Long problemId;
    private final String studentName;
    private final String courseTitle;
    private final String problemTitle;
    private final String question;
    private final String answer;
    private final String status;
    private final LocalDateTime createdAt;
    private final LocalDateTime answeredAt;
    private final List<DoubtAnswerResponse> answers;

    public DoubtResponse(Long id,
                         Long userId,
                         Long problemId,
                         String studentName,
                         String courseTitle,
                         String problemTitle,
                         String question,
                         String answer,
                         String status,
                         LocalDateTime createdAt,
                         LocalDateTime answeredAt,
                         List<DoubtAnswerResponse> answers) {
        this.id = id;
        this.userId = userId;
        this.problemId = problemId;
        this.studentName = studentName;
        this.courseTitle = courseTitle;
        this.problemTitle = problemTitle;
        this.question = question;
        this.answer = answer;
        this.status = status;
        this.createdAt = createdAt;
        this.answeredAt = answeredAt;
        this.answers = answers;
    }

    public static DoubtResponse from(Doubt doubt) {
        return from(doubt, false);
    }

    public static DoubtResponse fromWithAnswers(Doubt doubt) {
        return from(doubt, true);
    }

    private static DoubtResponse from(Doubt doubt, boolean includeAnswers) {
        List<DoubtAnswer> doubtAnswers = doubt.getAnswers();
        DoubtAnswer latestAnswer = doubtAnswers.isEmpty() ? null : doubtAnswers.get(doubtAnswers.size() - 1);

        String firstName = doubt.getLearner().getFirstName();
        String lastName = doubt.getLearner().getLastName();
        String fullName = ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
        String studentName = fullName.isEmpty() ? doubt.getLearner().getUsername() : fullName;

        return new DoubtResponse(
                doubt.getId(),
                doubt.getLearner().getId(),
                doubt.getProblem().getId(),
                studentName,
                "Practice",
                doubt.getProblem().getTitle(),
                doubt.getQuestion(),
                latestAnswer == null ? null : latestAnswer.getAnswer(),
                doubt.getStatus().name(),
                doubt.getCreatedAt(),
                latestAnswer == null ? null : latestAnswer.getCreatedAt(),
                includeAnswers
                        ? doubtAnswers.stream().map(DoubtAnswerResponse::from).toList()
                        : List.of());
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getProblemId() { return problemId; }
    public String getStudentName() { return studentName; }
    public String getCourseTitle() { return courseTitle; }
    public String getProblemTitle() { return problemTitle; }
    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getAnsweredAt() { return answeredAt; }
    public List<DoubtAnswerResponse> getAnswers() { return answers; }
}