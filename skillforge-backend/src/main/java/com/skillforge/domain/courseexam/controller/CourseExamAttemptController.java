package com.skillforge.domain.courseexam.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillforge.common.response.ApiResponse;
import com.skillforge.domain.courseexam.dto.CourseExamAttemptResultResponse;
import com.skillforge.domain.courseexam.dto.StartCourseExamResponse;
import com.skillforge.domain.courseexam.dto.SubmitCourseExamRequest;
import com.skillforge.domain.courseexam.service.CourseExamAttemptService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/courses/{courseId}/exams/{examId}")
public class CourseExamAttemptController {

    private final CourseExamAttemptService courseExamAttemptService;

    public CourseExamAttemptController(CourseExamAttemptService courseExamAttemptService) {
        this.courseExamAttemptService = courseExamAttemptService;
    }

    @PostMapping("/attempts/start")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<StartCourseExamResponse>> startExam(
            @PathVariable Long courseId,
            @PathVariable Long examId,
            @AuthenticationPrincipal UserDetails principal) {
        StartCourseExamResponse response = courseExamAttemptService.startExam(courseId, examId, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Course final exam started", response));
    }

    @PostMapping("/attempts/submit")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<CourseExamAttemptResultResponse>> submitExam(
            @PathVariable Long courseId,
            @PathVariable Long examId,
            @Valid @RequestBody SubmitCourseExamRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        CourseExamAttemptResultResponse response = courseExamAttemptService.submitExam(
                courseId,
                examId,
                request,
                principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Course final exam submitted", response));
    }
}
