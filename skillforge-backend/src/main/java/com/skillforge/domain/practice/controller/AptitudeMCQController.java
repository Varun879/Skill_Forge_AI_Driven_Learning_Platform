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
import com.skillforge.common.response.ApiResponse;
import com.skillforge.domain.practice.dto.AptitudeAnswerRequest;
import com.skillforge.domain.practice.dto.AptitudeAnswerResponse;
import com.skillforge.domain.practice.dto.PracticeQuestionResponse;
import com.skillforge.domain.practice.dto.PracticeRecommendationResponse;
import com.skillforge.domain.practice.service.AptitudeMCQService;

import jakarta.validation.Valid;

@RestController
@RequestMapping({"/api/practice", "/practice"})
public class AptitudeMCQController {

    private final AptitudeMCQService aptitudeMCQService;

    public AptitudeMCQController(AptitudeMCQService aptitudeMCQService) {
        this.aptitudeMCQService = aptitudeMCQService;
    }

    @GetMapping("/aptitude")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<List<PracticeQuestionResponse>>> getAptitudeQuestions(
            @RequestParam(required = false) DifficultyLevel difficulty,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "10") int limit) {
        List<PracticeQuestionResponse> questions = aptitudeMCQService.getAptitudeQuestions(difficulty, topic, tag, limit);
        return ResponseEntity.ok(ApiResponse.ok(questions));
    }

    @PostMapping("/aptitude/answer")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<AptitudeAnswerResponse>> answerAptitudeQuestion(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody AptitudeAnswerRequest request) {
        AptitudeAnswerResponse response = aptitudeMCQService.answerAptitudeQuestion(principal.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("Aptitude MCQ answer submitted", response));
    }

    @GetMapping("/aptitude/next")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<PracticeRecommendationResponse>> getNextAptitudeQuestion(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) String topic) {
        PracticeRecommendationResponse response = aptitudeMCQService.getNextAptitudeQuestion(principal.getUsername(), topic);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
