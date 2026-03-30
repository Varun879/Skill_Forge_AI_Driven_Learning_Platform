package com.skillforge.domain.ai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillforge.common.response.ApiResponse;
import com.skillforge.domain.ai.dto.AIChatRequest;
import com.skillforge.domain.ai.dto.AIChatResponse;
import com.skillforge.domain.ai.service.AIChatService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ai")
public class AIChatController {

    private final AIChatService aiChatService;

    public AIChatController(AIChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping("/chat")
    @PreAuthorize("hasAnyRole('LEARNER','TUTOR')")
    public ResponseEntity<ApiResponse<AIChatResponse>> chat(
            @Valid @RequestBody AIChatRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        AIChatResponse response = aiChatService.ask(request, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
