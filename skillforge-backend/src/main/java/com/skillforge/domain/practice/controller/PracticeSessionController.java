package com.skillforge.domain.practice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillforge.common.response.ApiResponse;
import com.skillforge.domain.practice.dto.PracticeSessionHistoryItemResponse;
import com.skillforge.domain.practice.dto.PracticeSessionStartRequest;
import com.skillforge.domain.practice.dto.PracticeSessionStartResponse;
import com.skillforge.domain.practice.dto.PracticeSessionSubmitRequest;
import com.skillforge.domain.practice.dto.PracticeSessionSubmitResponse;
import com.skillforge.domain.practice.service.PracticeSessionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping({"/api/practice", "/practice"})
public class PracticeSessionController {

    private final PracticeSessionService practiceSessionService;

    public PracticeSessionController(PracticeSessionService practiceSessionService) {
        this.practiceSessionService = practiceSessionService;
    }

    @PostMapping("/session/start")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<PracticeSessionStartResponse>> startSession(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody PracticeSessionStartRequest request) {
        PracticeSessionStartResponse response = practiceSessionService.startSession(principal.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("Practice session started", response));
    }

    @PostMapping("/session/submit")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<PracticeSessionSubmitResponse>> submitToSession(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody PracticeSessionSubmitRequest request) {
        PracticeSessionSubmitResponse response = practiceSessionService.submitToSession(principal.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("Practice session updated", response));
    }

    @GetMapping("/session/history")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<List<PracticeSessionHistoryItemResponse>>> getSessionHistory(
            @AuthenticationPrincipal UserDetails principal) {
        List<PracticeSessionHistoryItemResponse> response = practiceSessionService.getSessionHistory(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
