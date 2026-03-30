package com.skillforge.domain.submission.controller;

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
import com.skillforge.domain.submission.dto.ProvideFeedbackRequest;
import com.skillforge.domain.submission.dto.RunSubmissionRequest;
import com.skillforge.domain.submission.dto.SubmissionResultResponse;
import com.skillforge.domain.submission.dto.SubmissionViewResponse;
import com.skillforge.domain.submission.dto.SubmitSubmissionRequest;
import com.skillforge.domain.submission.service.SubmissionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping("/run")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<SubmissionResultResponse>> runSubmission(
            @Valid @RequestBody RunSubmissionRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        SubmissionResultResponse result = submissionService.runSubmission(request, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Submission executed", result));
    }

    @PostMapping("/submit")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<SubmissionResultResponse>> submitSubmission(
            @Valid @RequestBody SubmitSubmissionRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        SubmissionResultResponse result = submissionService.submitSubmission(request, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Submission saved", result));
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<List<SubmissionViewResponse>>> getUserSubmissions(
            @AuthenticationPrincipal UserDetails principal) {
        List<SubmissionViewResponse> submissions = submissionService.getUserSubmissions(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(submissions));
    }

    @GetMapping("/problem/{id}")
    @PreAuthorize("hasAnyRole('LEARNER','TUTOR')")
    public ResponseEntity<ApiResponse<List<SubmissionViewResponse>>> getSubmissionsByProblem(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        List<SubmissionViewResponse> submissions = submissionService.getSubmissionsByProblem(id, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(submissions));
    }

    @GetMapping("/review")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<ApiResponse<List<SubmissionViewResponse>>> getReviewSubmissions(
            @AuthenticationPrincipal UserDetails principal) {
        List<SubmissionViewResponse> submissions = submissionService.getReviewSubmissions(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(submissions));
    }

    @PostMapping("/feedback")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<ApiResponse<SubmissionViewResponse>> provideFeedback(
            @Valid @RequestBody ProvideFeedbackRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        SubmissionViewResponse submission = submissionService.provideFeedback(request, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Feedback submitted", submission));
    }
}
