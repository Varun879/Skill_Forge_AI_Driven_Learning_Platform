package com.skillforge.domain.practice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillforge.common.response.ApiResponse;
import com.skillforge.domain.practice.dto.PracticeLeaderboardResponse;
import com.skillforge.domain.practice.service.PracticeLeaderboardService;

@RestController
@RequestMapping("/api/leaderboard")
public class PracticeLeaderboardController {

    private final PracticeLeaderboardService practiceLeaderboardService;

    public PracticeLeaderboardController(PracticeLeaderboardService practiceLeaderboardService) {
        this.practiceLeaderboardService = practiceLeaderboardService;
    }

    @GetMapping("/practice")
    public ResponseEntity<ApiResponse<PracticeLeaderboardResponse>> getPracticeLeaderboard(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "20") int limit) {
        String currentUserEmail = principal == null ? null : principal.getUsername();
        PracticeLeaderboardResponse response = practiceLeaderboardService.getPracticeLeaderboard(currentUserEmail, limit);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
