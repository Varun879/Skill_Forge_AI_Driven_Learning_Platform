package com.skillforge.domain.problem.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillforge.common.enums.DifficultyLevel;
import com.skillforge.common.response.ApiResponse;
import com.skillforge.domain.problem.dto.CreateProblemRequest;
import com.skillforge.domain.problem.dto.ProblemListResponse;
import com.skillforge.domain.problem.dto.ProblemResponse;
import com.skillforge.domain.problem.dto.SubmissionResponse;
import com.skillforge.domain.problem.dto.SubmitSolutionRequest;
import com.skillforge.domain.problem.dto.UpdateProblemRequest;
import com.skillforge.domain.problem.service.ProblemService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<ApiResponse<ProblemResponse>> createProblem(
            @Valid @RequestBody CreateProblemRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        ProblemResponse created = problemService.createProblem(request, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Problem created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<ApiResponse<ProblemResponse>> updateProblem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProblemRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        ProblemResponse updated = problemService.updateProblem(id, request, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Problem updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<ApiResponse<Void>> deleteProblem(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        problemService.deleteProblem(id, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Problem deleted successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProblemListResponse>>> getProblems(
            @RequestParam(required = false) DifficultyLevel difficulty) {
        List<ProblemListResponse> problems = problemService.getProblems(difficulty);
        return ResponseEntity.ok(ApiResponse.ok(problems));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProblemResponse>> getProblemById(@PathVariable Long id) {
        ProblemResponse problem = problemService.getProblemById(id);
        return ResponseEntity.ok(ApiResponse.ok(problem));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<SubmissionResponse>> submitSolution(
            @PathVariable Long id,
            @Valid @RequestBody SubmitSolutionRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        SubmissionResponse submission = problemService.submitSolution(id, request, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Solution submitted", submission));
    }
}
