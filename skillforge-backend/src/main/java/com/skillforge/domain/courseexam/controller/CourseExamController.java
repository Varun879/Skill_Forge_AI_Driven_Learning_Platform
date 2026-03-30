package com.skillforge.domain.courseexam.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillforge.common.response.ApiResponse;
import com.skillforge.domain.courseexam.dto.CourseExamResponse;
import com.skillforge.domain.courseexam.dto.CreateCourseExamRequest;
import com.skillforge.domain.courseexam.service.CourseExamService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/courses")
public class CourseExamController {

    private final CourseExamService courseExamService;

    public CourseExamController(CourseExamService courseExamService) {
        this.courseExamService = courseExamService;
    }

    @PostMapping("/{courseId}/exams")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<ApiResponse<CourseExamResponse>> createCourseExam(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateCourseExamRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        CourseExamResponse response = courseExamService.createExam(courseId, request, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Course exam created successfully", response));
    }

    @GetMapping("/{courseId}/exams")
    public ResponseEntity<ApiResponse<List<CourseExamResponse>>> listCourseExams(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails principal) {
        String requesterEmail = principal != null ? principal.getUsername() : null;
        List<CourseExamResponse> response = courseExamService.listExams(courseId, requesterEmail);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
