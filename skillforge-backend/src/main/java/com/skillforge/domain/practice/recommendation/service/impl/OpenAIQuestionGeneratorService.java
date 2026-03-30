package com.skillforge.domain.practice.recommendation.service.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillforge.common.enums.MCQDifficulty;
import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.domain.practice.entity.AptitudeMCQ;
import com.skillforge.domain.practice.entity.MCQOption;
import com.skillforge.domain.practice.entity.PracticeQuestion;
import com.skillforge.domain.practice.entity.ProgrammingMCQ;
import com.skillforge.domain.practice.recommendation.dto.GeneratedMCQDto;
import com.skillforge.domain.practice.recommendation.service.AIQuestionGeneratorService;
import com.skillforge.domain.practice.repository.AptitudeMCQRepository;
import com.skillforge.domain.practice.repository.MCQOptionRepository;
import com.skillforge.domain.practice.repository.PracticeQuestionRepository;
import com.skillforge.domain.practice.repository.ProgrammingMCQRepository;

/**
 * AI-powered MCQ generator that calls an OpenAI-compatible Chat Completions
 * API to produce questions, then persists them in the existing practice tables.
 *
 * <p>If the API key is absent or the call fails the method logs a warning and
 * returns without throwing — the calling code falls back gracefully.</p>
 *
 * <p>This service is <em>additive only</em>: it only inserts new rows and
 * never modifies or deletes existing questions.</p>
 */
@Service
public class OpenAIQuestionGeneratorService implements AIQuestionGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(OpenAIQuestionGeneratorService.class);

    /** Letters used in the correctOption field returned by the AI model. */
    private static final List<String> OPTION_LETTERS = Arrays.asList("A", "B", "C", "D");
    private static final String OPENAI_DEFAULT_URL = "https://api.openai.com/v1/chat/completions";
    private static final String GEMINI_OPENAI_COMPAT_URL = "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions";

    @Value("${practice.ai.model-api-key:${AI_MODEL_API_KEY:${GEMINI_API_KEY:${GOOGLE_API_KEY:${VITE_GEMINI_API_KEY:}}}}}")
    private String apiKey;

    @Value("${practice.ai.model-api-url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${practice.ai.model-name:gpt-4.1-mini}")
    private String modelName;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PracticeQuestionRepository questionRepository;
    private final ProgrammingMCQRepository programmingMCQRepository;
    private final AptitudeMCQRepository aptitudeMCQRepository;
    private final MCQOptionRepository mcqOptionRepository;

    public OpenAIQuestionGeneratorService(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            PracticeQuestionRepository questionRepository,
            ProgrammingMCQRepository programmingMCQRepository,
            AptitudeMCQRepository aptitudeMCQRepository,
            MCQOptionRepository mcqOptionRepository) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.questionRepository = questionRepository;
        this.programmingMCQRepository = programmingMCQRepository;
        this.aptitudeMCQRepository = aptitudeMCQRepository;
        this.mcqOptionRepository = mcqOptionRepository;
    }

    @Override
    @Transactional
    public void generateAndSave(String category, PracticeQuestionType questionType, int count) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("AI_MODEL_API_KEY is not configured — skipping question generation "
                    + "[category={}, type={}]", category, questionType);
            return;
        }

        List<GeneratedMCQDto> generated = callAI(category, questionType, count);
        if (generated == null || generated.isEmpty()) {
            log.warn("AI returned no questions [category={}, type={}]", category, questionType);
            return;
        }

        int saved = 0;
        for (GeneratedMCQDto dto : generated) {
            try {
                persistQuestion(dto, questionType);
                saved++;
            } catch (Exception ex) {
                log.error("Failed to persist AI-generated question [category={}, question={}]: {}",
                        category, dto.getQuestion(), ex.getMessage());
            }
        }
        log.info("AI generation complete: {} questions saved [category={}, type={}]",
                saved, category, questionType);
    }

    // ------------------------------------------------------------------
    // AI call
    // ------------------------------------------------------------------

    private List<GeneratedMCQDto> callAI(String category, PracticeQuestionType questionType, int count) {
        String questionTypeName = questionType == PracticeQuestionType.PROGRAMMING_MCQ
                ? "Programming MCQ" : "Aptitude MCQ";
        String resolvedApiUrl = resolveApiUrl();
        String resolvedModelName = resolveModelName(resolvedApiUrl);

        String prompt = buildPrompt(category, questionTypeName, count);

        Map<String, Object> requestBody = Map.of(
            "model", resolvedModelName,
                "messages", List.of(
                        Map.of("role", "system",
                               "content", "You are an expert educational MCQ generator. "
                                       + "Always respond with valid JSON only, no markdown fences."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            String rawResponse = restTemplate.postForObject(resolvedApiUrl, request, String.class);
            return parseAIResponse(rawResponse);
        } catch (RestClientException ex) {
            log.error("AI API request failed [category={}, model={}, url={}]: {}",
                    category, resolvedModelName, resolvedApiUrl, ex.getMessage());
            return List.of();
        }
    }

    private String resolveApiUrl() {
        if (apiUrl == null || apiUrl.isBlank()) {
            return OPENAI_DEFAULT_URL;
        }

        if (OPENAI_DEFAULT_URL.equalsIgnoreCase(apiUrl) && isGeminiApiKey(apiKey)) {
            return GEMINI_OPENAI_COMPAT_URL;
        }

        return apiUrl;
    }

    private String resolveModelName(String resolvedApiUrl) {
        if (resolvedApiUrl != null
                && resolvedApiUrl.toLowerCase().contains("generativelanguage.googleapis.com")
                && (modelName == null || modelName.isBlank() || modelName.toLowerCase().startsWith("gpt-"))) {
            return "gemini-2.0-flash";
        }

        return (modelName == null || modelName.isBlank()) ? "gpt-4.1-mini" : modelName;
    }

    private boolean isGeminiApiKey(String value) {
        return value != null && value.startsWith("AIza");
    }

    private String buildPrompt(String category, String questionTypeName, int count) {
        return String.format("""
                Generate %d multiple choice questions.

                Category: %s
                Type: %s
                Difficulty levels: mix of EASY, MEDIUM, HARD

                Return a JSON array in this exact format:
                [
                  {
                    "question": "...",
                    "category": "%s",
                    "difficulty": "EASY|MEDIUM|HARD",
                    "options": ["option text A", "option text B", "option text C", "option text D"],
                    "correctOption": "A|B|C|D",
                    "explanation": "...",
                    "estimatedSolveTime": 60
                  }
                ]

                Rules:
                - options must have exactly 4 entries
                - correctOption must be one of: A, B, C, D
                - estimatedSolveTime is in seconds
                - Return only valid JSON, no markdown
                """, count, category, questionTypeName, category);
    }

    private List<GeneratedMCQDto> parseAIResponse(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        try {
            JsonNode root = objectMapper.readTree(raw);
            // OpenAI response: root.choices[0].message.content
            String content;
            if (root.has("choices")) {
                content = root.path("choices").get(0).path("message").path("content").asText();
            } else {
                // Might already be a JSON array (non-OpenAI compatible endpoint)
                content = raw;
            }
            content = normalizeJsonContent(content);
            return objectMapper.readValue(content, new TypeReference<List<GeneratedMCQDto>>() {});
        } catch (JsonProcessingException ex) {
            log.error("Failed to parse AI response: {}", ex.getMessage());
            return List.of();
        }
    }

    private String normalizeJsonContent(String content) {
        if (content == null) {
            return "";
        }

        String normalized = content.trim();
        if (normalized.startsWith("```")) {
            normalized = normalized.replaceFirst("^```(?:json)?\\s*", "");
            normalized = normalized.replaceFirst("\\s*```$", "");
            normalized = normalized.trim();
        }

        if (!(normalized.startsWith("[") || normalized.startsWith("{"))) {
            int arrayStart = normalized.indexOf('[');
            int arrayEnd = normalized.lastIndexOf(']');
            if (arrayStart >= 0 && arrayEnd > arrayStart) {
                normalized = normalized.substring(arrayStart, arrayEnd + 1);
            }
        }

        return normalized;
    }

    // ------------------------------------------------------------------
    // Persistence
    // ------------------------------------------------------------------

    private void persistQuestion(GeneratedMCQDto dto, PracticeQuestionType questionType) {
        MCQDifficulty difficulty = MCQDifficulty.parseOrDefault(dto.getDifficulty());

        // Build PracticeQuestion
        PracticeQuestion pq = new PracticeQuestion();
        pq.setQuestionType(questionType);
        pq.setTitle(truncate(dto.getQuestion(), 250));
        pq.setPrompt(dto.getQuestion());
        pq.setDifficultyLevel(difficulty.toDifficultyLevel());
        pq.setTopic(dto.getCategory() != null ? dto.getCategory() : "Unknown");
        pq.setTags(Set.of(dto.getCategory() != null ? dto.getCategory() : "ai-generated"));
        // Convert seconds → minutes (round up, minimum 1)
        int estMinutes = Math.max(1, (int) Math.ceil(dto.getEstimatedSolveTime() / 60.0));
        pq.setEstimatedSolveTimeMinutes(estMinutes);
        pq.setSuccessRate(BigDecimal.ZERO);
        pq.setIsActive(true);

        pq = questionRepository.save(pq);

        // Save extension row (explanation storage)
        if (questionType == PracticeQuestionType.PROGRAMMING_MCQ) {
            ProgrammingMCQ ext = new ProgrammingMCQ();
            ext.setQuestion(pq);
            ext.setExplanation(dto.getExplanation());
            programmingMCQRepository.save(ext);
        } else {
            AptitudeMCQ ext = new AptitudeMCQ();
            ext.setQuestion(pq);
            ext.setExplanation(dto.getExplanation());
            aptitudeMCQRepository.save(ext);
        }

        // Save options
        List<String> options = dto.getOptions();
        if (options == null || options.size() < 2) return;

        int correctIndex = letterToIndex(dto.getCorrectOption());

        for (int i = 0; i < options.size(); i++) {
            MCQOption opt = new MCQOption();
            opt.setQuestion(pq);
            opt.setOptionText(options.get(i));
            opt.setDisplayOrder(i + 1);
            opt.setIsCorrect(i == correctIndex);
            mcqOptionRepository.save(opt);
        }
    }

    /** Converts "A"→0, "B"→1, "C"→2, "D"→3. Returns 0 for unknown letters. */
    private int letterToIndex(String letter) {
        if (letter == null) return 0;
        int idx = OPTION_LETTERS.indexOf(letter.trim().toUpperCase());
        return idx < 0 ? 0 : idx;
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen);
    }
}
