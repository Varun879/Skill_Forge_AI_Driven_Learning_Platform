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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillforge.common.enums.DifficultyLevel;
import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.common.response.ApiResponse;
import com.skillforge.domain.practice.dto.PracticeAnswerRequest;
import com.skillforge.domain.practice.dto.PracticeAnswerResponse;
import com.skillforge.domain.practice.dto.PracticeQuestionResponse;
import com.skillforge.domain.practice.dto.PracticeRecommendationResponse;
import com.skillforge.domain.practice.dto.PracticeStatsOverviewResponse;
import com.skillforge.domain.practice.service.PracticeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/practice")
public class PracticeController {

    private final PracticeService practiceService;

    public PracticeController(PracticeService practiceService) {
        this.practiceService = practiceService;
    }

    @GetMapping("/questions")
    public ResponseEntity<ApiResponse<List<PracticeQuestionResponse>>> getQuestions(
            @RequestParam(required = false) PracticeQuestionType type,
            @RequestParam(required = false) DifficultyLevel difficulty,
            @RequestParam(required = false) String topic) {
        List<PracticeQuestionResponse> questions = practiceService.getQuestions(type, difficulty, topic);
        return ResponseEntity.ok(ApiResponse.ok(questions));
    }

    @GetMapping("/recommendation")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<PracticeRecommendationResponse>> getRecommendation(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) PracticeQuestionType type,
            @RequestParam(required = false) String topic) {
        PracticeRecommendationResponse recommendation = practiceService.getRecommendation(
                principal.getUsername(),
                type,
                topic);
        return ResponseEntity.ok(ApiResponse.ok(recommendation));
    }

    @GetMapping("/stats/overview")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<PracticeStatsOverviewResponse>> getStatsOverview(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "30") int days) {
        PracticeStatsOverviewResponse overview = practiceService.getStatsOverview(principal.getUsername(), days);
        return ResponseEntity.ok(ApiResponse.ok(overview));
    }

    @PostMapping("/answers")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<PracticeAnswerResponse>> submitAnswer(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody PracticeAnswerRequest request) {
        PracticeAnswerResponse response = practiceService.submitAnswer(principal.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("Practice answer submitted successfully", response));
    }
}
