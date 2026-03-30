package com.skillforge.domain.course.dto;

import java.time.LocalDateTime;

import com.skillforge.domain.course.entity.CourseEnrollment;

public class EnrollmentResponse {

    private Long enrollmentId;
    private Long courseId;
    private String courseTitle;
    private Long learnerId;
    private LocalDateTime enrolledAt;

    private EnrollmentResponse() {}

    public static EnrollmentResponse from(CourseEnrollment enrollment) {
        EnrollmentResponse r = new EnrollmentResponse();
        r.enrollmentId = enrollment.getId();
        r.courseId     = enrollment.getCourse().getId();
        r.courseTitle  = enrollment.getCourse().getTitle();
        r.learnerId    = enrollment.getLearner().getId();
        r.enrolledAt   = enrollment.getEnrolledAt();
        return r;
    }

    public Long getEnrollmentId()          { return enrollmentId; }
    public Long getCourseId()              { return courseId; }
    public String getCourseTitle()         { return courseTitle; }
    public Long getLearnerId()             { return learnerId; }
    public LocalDateTime getEnrolledAt()   { return enrolledAt; }
}
