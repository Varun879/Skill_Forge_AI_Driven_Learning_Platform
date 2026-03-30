package com.skillforge.domain.doubt.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillforge.common.response.ApiResponse;
import com.skillforge.domain.doubt.dto.AnswerDoubtRequest;
import com.skillforge.domain.doubt.dto.CreateDoubtRequest;
import com.skillforge.domain.doubt.dto.DoubtResponse;
import com.skillforge.domain.doubt.service.DoubtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/doubts")
public class DoubtController {

    private final DoubtService doubtService;

    public DoubtController(DoubtService doubtService) {
        this.doubtService = doubtService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('LEARNER','TUTOR')")
    public ResponseEntity<ApiResponse<List<DoubtResponse>>> getDoubts(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) Long problemId,
            @RequestParam(required = false) String status) {
        List<DoubtResponse> doubts = doubtService.getDoubts(principal.getUsername(), problemId, status);
        return ResponseEntity.ok(ApiResponse.ok(doubts));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LEARNER','TUTOR')")
    public ResponseEntity<ApiResponse<DoubtResponse>> getDoubt(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        DoubtResponse doubt = doubtService.getDoubt(id, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(doubt));
    }

    @PostMapping
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<DoubtResponse>> createDoubt(
            @Valid @RequestBody CreateDoubtRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        DoubtResponse doubt = doubtService.createDoubt(request, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Doubt submitted", doubt));
    }

    @PostMapping("/{id}/answer")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<ApiResponse<DoubtResponse>> answerDoubt(
            @PathVariable Long id,
            @Valid @RequestBody AnswerDoubtRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        DoubtResponse doubt = doubtService.answerDoubt(id, request, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Doubt answered", doubt));
    }
}