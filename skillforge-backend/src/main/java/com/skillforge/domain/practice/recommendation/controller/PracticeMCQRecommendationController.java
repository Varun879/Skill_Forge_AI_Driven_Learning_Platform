package com.skillforge.domain.practice.recommendation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillforge.common.response.ApiResponse;
import com.skillforge.domain.practice.recommendation.dto.CategoryPerformanceDto;
import com.skillforge.domain.practice.recommendation.dto.MCQCategoryQuestionsResponse;
import com.skillforge.domain.practice.recommendation.dto.MCQTypeParam;
import com.skillforge.domain.practice.recommendation.dto.NextQuestionResponse;
import com.skillforge.domain.practice.recommendation.dto.NextQuestionSetResponse;
import com.skillforge.domain.practice.recommendation.service.MCQCategoryQueryService;
import com.skillforge.domain.practice.recommendation.service.NextMCQRecommendationService;
import com.skillforge.domain.practice.recommendation.service.UserPerformanceAnalyzer;
import com.skillforge.domain.user.entity.User;
import com.skillforge.domain.user.repository.UserRepository;
import com.skillforge.exception.BadRequestException;
import com.skillforge.exception.ResourceNotFoundException;

/**
 * New isolated endpoint for adaptive MCQ recommendation.
 * Supports both route styles to match client integrations:
 * - /api/practice/mcq/next
 * - /practice/mcq/next
 */
@RestController
@RequestMapping({"/api/practice", "/practice"})
public class PracticeMCQRecommendationController {

    private final NextMCQRecommendationService nextMCQRecommendationService;
    private final MCQCategoryQueryService mcqCategoryQueryService;
    private final UserPerformanceAnalyzer userPerformanceAnalyzer;
    private final UserRepository userRepository;

    public PracticeMCQRecommendationController(
            NextMCQRecommendationService nextMCQRecommendationService,
            MCQCategoryQueryService mcqCategoryQueryService,
            UserPerformanceAnalyzer userPerformanceAnalyzer,
            UserRepository userRepository) {
        this.nextMCQRecommendationService = nextMCQRecommendationService;
        this.mcqCategoryQueryService = mcqCategoryQueryService;
        this.userPerformanceAnalyzer = userPerformanceAnalyzer;
        this.userRepository = userRepository;
    }

    @GetMapping("/mcq/categories")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<List<MCQCategoryQuestionsResponse>>> categories(
            @RequestParam String type) {

        MCQTypeParam parsedType;
        try {
            parsedType = MCQTypeParam.fromValue(type);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(ex.getMessage());
        }

        var questionType = parsedType.toPracticeQuestionType();
        List<MCQCategoryQuestionsResponse> response = mcqCategoryQueryService.getCategoryQuestions(questionType);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/mcq/next")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<NextQuestionResponse>> next(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam String type,
            @RequestParam(required = false) String category) {

        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getUsername()));

        MCQTypeParam parsedType;
        try {
            parsedType = MCQTypeParam.fromValue(type);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(ex.getMessage());
        }

        NextQuestionResponse response = nextMCQRecommendationService.getNext(user.getId(), parsedType, category);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/mcq/next-set")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<NextQuestionSetResponse>> nextSet(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam String type,
            @RequestParam String category,
            @RequestParam(defaultValue = "10") int size) {

        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getUsername()));

        MCQTypeParam parsedType;
        try {
            parsedType = MCQTypeParam.fromValue(type);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(ex.getMessage());
        }

        NextQuestionSetResponse response = nextMCQRecommendationService.getNextSet(user.getId(), parsedType, category, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/mcq/performance")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<List<CategoryPerformanceDto>>> performance(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam String type) {

        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getUsername()));

        MCQTypeParam parsedType;
        try {
            parsedType = MCQTypeParam.fromValue(type);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(ex.getMessage());
        }

        List<CategoryPerformanceDto> response = userPerformanceAnalyzer
                .getPerformanceWithAllCategories(user.getId(), parsedType.toPracticeQuestionType());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
