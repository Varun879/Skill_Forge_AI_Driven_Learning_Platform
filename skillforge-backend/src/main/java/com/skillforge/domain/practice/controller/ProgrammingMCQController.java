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
import com.skillforge.domain.practice.dto.PracticeQuestionResponse;
import com.skillforge.domain.practice.dto.PracticeRecommendationResponse;
import com.skillforge.domain.practice.dto.ProgrammingMCQAnswerRequest;
import com.skillforge.domain.practice.dto.ProgrammingMCQAnswerResponse;
import com.skillforge.domain.practice.service.ProgrammingMCQService;

import jakarta.validation.Valid;

@RestController
@RequestMapping({"/api/practice", "/practice"})
public class ProgrammingMCQController {

    private final ProgrammingMCQService programmingMCQService;

    public ProgrammingMCQController(ProgrammingMCQService programmingMCQService) {
        this.programmingMCQService = programmingMCQService;
    }

    @GetMapping("/programming-mcq")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<List<PracticeQuestionResponse>>> getProgrammingMcqs(
            @RequestParam(required = false) DifficultyLevel difficulty,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "10") int limit) {
        List<PracticeQuestionResponse> questions = programmingMCQService.getProgrammingMcqs(difficulty, topic, tag, limit);
        return ResponseEntity.ok(ApiResponse.ok(questions));
    }

    @PostMapping("/programming-mcq/answer")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<ProgrammingMCQAnswerResponse>> answerProgrammingMcq(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ProgrammingMCQAnswerRequest request) {
        ProgrammingMCQAnswerResponse response = programmingMCQService.answerProgrammingMcq(principal.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("Programming MCQ answer submitted", response));
    }

    @GetMapping("/programming-mcq/next")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<PracticeRecommendationResponse>> getNextProgrammingMcq(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) String topic) {
        PracticeRecommendationResponse response = programmingMCQService.getNextProgrammingMcq(principal.getUsername(), topic);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
