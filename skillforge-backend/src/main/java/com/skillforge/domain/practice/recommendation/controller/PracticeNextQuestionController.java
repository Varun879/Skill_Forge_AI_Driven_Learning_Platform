package com.skillforge.domain.practice.recommendation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.common.response.ApiResponse;
import com.skillforge.domain.practice.recommendation.dto.NextQuestionResponse;
import com.skillforge.domain.practice.recommendation.service.NextQuestionRecommendationService;
import com.skillforge.domain.user.entity.User;
import com.skillforge.domain.user.repository.UserRepository;
import com.skillforge.exception.ResourceNotFoundException;

/**
 * Exposes the AI-powered next-question endpoint for the Practice section.
 *
 * <pre>
 * GET /api/practice/next-question?questionType=PROGRAMMING_MCQ
 * GET /api/practice/next-question?questionType=APTITUDE_MCQ
 * </pre>
 *
 * <p>Authentication: LEARNER role required (JWT).
 * The userId is resolved from the authenticated principal — clients must NOT
 * pass userId as a query parameter.</p>
 *
 * <p>This controller is isolated in the {@code recommendation} sub-package
 * and does NOT modify the existing {@link com.skillforge.domain.practice.controller.PracticeController}.</p>
 */
@RestController
@RequestMapping("/api/practice")
public class PracticeNextQuestionController {

    private final NextQuestionRecommendationService recommendationService;
    private final UserRepository userRepository;

    public PracticeNextQuestionController(
            NextQuestionRecommendationService recommendationService,
            UserRepository userRepository) {
        this.recommendationService = recommendationService;
        this.userRepository = userRepository;
    }

    /**
     * Returns the best next question for the authenticated learner.
     *
     * <p>Full pipeline:
     * <ol>
     *   <li>Ensure every category has ≥ 10 questions (AI generation if needed).</li>
     *   <li>Analyse the user's per-category performance.</li>
     *   <li>Select from: weak categories → medium categories → new categories → fallback.</li>
     *   <li>Adjust difficulty based on the user's last answer in the chosen category.</li>
     * </ol>
     *
     * @param principal    the authenticated user (resolved by Spring Security)
     * @param questionType PROGRAMMING_MCQ or APTITUDE_MCQ
     * @return 200 with the selected question, 400 if questionType is invalid,
     *         404 if no question is available
     */
    @GetMapping("/next-question")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<NextQuestionResponse>> getNextQuestion(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam PracticeQuestionType questionType) {

        if (questionType == PracticeQuestionType.CODING) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("questionType must be PROGRAMMING_MCQ or APTITUDE_MCQ"));
        }

        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + principal.getUsername()));

        NextQuestionResponse response = recommendationService.recommend(user.getId(), questionType);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
