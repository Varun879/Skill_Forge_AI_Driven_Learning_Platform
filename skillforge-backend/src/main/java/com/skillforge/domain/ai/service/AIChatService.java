package com.skillforge.domain.ai.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillforge.domain.ai.dto.AIChatRequest;
import com.skillforge.domain.ai.dto.AIChatResponse;
import com.skillforge.domain.practice.entity.PracticeQuestion;
import com.skillforge.domain.practice.repository.PracticeQuestionRepository;
import com.skillforge.domain.user.entity.User;
import com.skillforge.domain.user.repository.UserRepository;
import com.skillforge.exception.BadRequestException;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.exception.UnauthorizedException;

@Service
public class AIChatService {

    private final UserRepository userRepository;
    private final PracticeQuestionRepository questionRepository;
    private final AIChatRateLimiter rateLimiter;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.chat.model-api-key:${practice.ai.model-api-key:}}")
    private String apiKey;

    @Value("${ai.chat.model-api-url:${practice.ai.model-api-url:https://generativelanguage.googleapis.com/v1beta/openai/chat/completions}}")
    private String apiUrl;

    @Value("${ai.chat.model-name:${practice.ai.model-name:gemini-2.0-flash}}")
    private String modelName;

    public AIChatService(UserRepository userRepository,
                         PracticeQuestionRepository questionRepository,
                         AIChatRateLimiter rateLimiter,
                         RestTemplate restTemplate,
                         ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.rateLimiter = rateLimiter;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public AIChatResponse ask(AIChatRequest request, String userEmail) {
        User current = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getUserId() != null && !request.getUserId().equals(current.getId())) {
            throw new UnauthorizedException("Cross-user AI access is not allowed");
        }

        rateLimiter.assertAllowed(current.getId());

        String userMessage = request.getMessage() == null ? "" : request.getMessage().trim();
        if (userMessage.isEmpty()) {
            throw new BadRequestException("Message cannot be empty");
        }

        if (request.getQuestionId() != null) {
            PracticeQuestion question = questionRepository.findByIdAndIsActiveTrue(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
            String reply = askHint(question, userMessage);
            return new AIChatResponse(sanitize(reply), true, question.getId());
        }

        String reply = askGeneral(userMessage);
        return new AIChatResponse(sanitize(reply), false, null);
    }

    private String askHint(PracticeQuestion question, String userMessage) {
        String prompt = "Question title: " + question.getTitle() + "\n"
            + "Question prompt: " + question.getPrompt() + "\n"
            + "Topic: " + question.getTopic() + "\n"
            + "Difficulty: " + question.getDifficultyLevel().name() + "\n"
            + "Student message: " + userMessage + "\n"
            + "Respond with a short hint strategy only. Do NOT reveal the final answer or correct option letter.";
        return callModel(prompt, true);
    }

    private String askGeneral(String userMessage) {
        String prompt = "Student question: " + userMessage + "\n"
            + "Give a concise concept explanation and learning guidance."
            + " Avoid giving direct solved MCQ answers or option letters.";
        return callModel(prompt, false);
    }

    private String callModel(String prompt, boolean hintMode) {
        if (apiKey == null || apiKey.isBlank()) {
            return hintMode
                ? "Hint: break the problem into smaller steps, identify constraints, and eliminate clearly wrong options first."
                : "Try grounding your approach in definitions, then solve a small example and validate each assumption.";
        }

        try {
            Map<String, Object> requestBody = Map.of(
                "model", modelName,
                "messages", List.of(
                    Map.of("role", "system", "content",
                        hintMode
                            ? "You are a tutor. Never provide final MCQ answers, correct option letters, or complete solved outputs. Provide hints only."
                            : "You are a tutor. Explain concepts clearly but never provide final MCQ answer keys or direct option letters."),
                    Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.4
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            String raw = restTemplate.postForObject(apiUrl, new HttpEntity<>(requestBody, headers), String.class);
            if (raw == null || raw.isBlank()) {
                throw new IllegalStateException("Empty AI response");
            }
            JsonNode root = objectMapper.readTree(raw);
            if (root.has("choices") && root.path("choices").isArray() && root.path("choices").size() > 0) {
                return root.path("choices").get(0).path("message") .path("content").asText("");
            }
            return raw;
        } catch (Exception ignored) {
            return hintMode
                ? "Hint: focus on the core pattern, test edge cases, and reason why each option fails before choosing one."
                : "Start from first principles, write down what is known, and verify your reasoning with a small example.";
        }
    }

    private String sanitize(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) {
            return "Try an incremental approach: identify inputs, expected output, and constraints, then test with a small example.";
        }

        String lower = value.toLowerCase();
        if (lower.contains("correct option") || lower.contains("the answer is") || lower.contains("option ")) {
            return "I can help with hints only: compare constraints, eliminate invalid options, and validate your chosen approach on a small test case.";
        }

        return value;
    }
}
