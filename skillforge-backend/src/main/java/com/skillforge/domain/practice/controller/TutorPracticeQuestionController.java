package com.skillforge.domain.practice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillforge.common.response.ApiResponse;
import com.skillforge.domain.practice.dto.PracticeQuestionResponse;
import com.skillforge.domain.practice.dto.TutorCreateAptitudeMcqRequest;
import com.skillforge.domain.practice.dto.TutorCreateCodingProblemRequest;
import com.skillforge.domain.practice.dto.TutorCreateProgrammingMcqRequest;
import com.skillforge.domain.practice.service.TutorPracticeQuestionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping({"/api/tutor", "/tutor"})
@PreAuthorize("hasRole('TUTOR')")
public class TutorPracticeQuestionController {

    private final TutorPracticeQuestionService tutorPracticeQuestionService;

    public TutorPracticeQuestionController(TutorPracticeQuestionService tutorPracticeQuestionService) {
        this.tutorPracticeQuestionService = tutorPracticeQuestionService;
    }

    @PostMapping("/problems")
    public ResponseEntity<ApiResponse<PracticeQuestionResponse>> createCodingProblem(
            @Valid @RequestBody TutorCreateCodingProblemRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        PracticeQuestionResponse response = tutorPracticeQuestionService.createCodingProblem(request, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Coding practice problem created", response));
    }

    @PostMapping("/programming-mcq")
    public ResponseEntity<ApiResponse<PracticeQuestionResponse>> createProgrammingMcq(
            @Valid @RequestBody TutorCreateProgrammingMcqRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        PracticeQuestionResponse response = tutorPracticeQuestionService.createProgrammingMcq(request, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Programming MCQ created", response));
    }

    @PostMapping("/aptitude-mcq")
    public ResponseEntity<ApiResponse<PracticeQuestionResponse>> createAptitudeMcq(
            @Valid @RequestBody TutorCreateAptitudeMcqRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        PracticeQuestionResponse response = tutorPracticeQuestionService.createAptitudeMcq(request, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Aptitude MCQ created", response));
    }
}
