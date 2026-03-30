package com.skillforge.domain.exam.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillforge.common.response.ApiResponse;
import com.skillforge.domain.exam.dto.ExamResultResponse;
import com.skillforge.domain.exam.dto.StartExamResponse;
import com.skillforge.domain.exam.dto.SubmitExamRequest;
import com.skillforge.domain.exam.service.ExamService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/exam")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @PostMapping("/start")
    @PreAuthorize("hasAnyRole('LEARNER','TUTOR')")
    public ResponseEntity<ApiResponse<StartExamResponse>> startExam(
            @AuthenticationPrincipal UserDetails principal) {
        StartExamResponse response = examService.startExam(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Exam started", response));
    }

    @PostMapping("/submit")
    @PreAuthorize("hasAnyRole('LEARNER','TUTOR')")
    public ResponseEntity<ApiResponse<ExamResultResponse>> submitExam(
            @Valid @RequestBody SubmitExamRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        ExamResultResponse response = examService.submitExam(request, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Exam submitted", response));
    }

    @GetMapping("/result")
    @PreAuthorize("hasAnyRole('LEARNER','TUTOR')")
    public ResponseEntity<ApiResponse<ExamResultResponse>> getResult(
            @RequestParam(required = false) Long sessionId,
            @AuthenticationPrincipal UserDetails principal) {
        ExamResultResponse response = examService.getResult(sessionId, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
