package com.skillforge.domain.execution.controller;

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
import com.skillforge.domain.execution.dto.ExecutionEnqueueResponse;
import com.skillforge.domain.execution.dto.ExecutionRequest;
import com.skillforge.domain.execution.dto.ExecutionResultResponse;
import com.skillforge.domain.execution.service.ExecutionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/execution")
public class ExecutionController {

    private final ExecutionService executionService;

    public ExecutionController(ExecutionService executionService) {
        this.executionService = executionService;
    }

    @PostMapping("/run")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<ExecutionEnqueueResponse>> run(
            @Valid @RequestBody ExecutionRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        ExecutionEnqueueResponse response = executionService.enqueueRun(request, principal.getUsername());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.ok("Execution queued", response));
    }

    @PostMapping("/submit")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<ExecutionEnqueueResponse>> submit(
            @Valid @RequestBody ExecutionRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        ExecutionEnqueueResponse response = executionService.enqueueSubmit(request, principal.getUsername());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.ok("Submission queued", response));
    }

    @GetMapping("/{id}/result")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<ExecutionResultResponse>> getResult(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        ExecutionResultResponse response = executionService.getResult(id, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
