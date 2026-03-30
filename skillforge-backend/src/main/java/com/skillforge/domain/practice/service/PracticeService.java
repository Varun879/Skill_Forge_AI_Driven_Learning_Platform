package com.skillforge.domain.practice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillforge.common.enums.DifficultyLevel;
import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.domain.practice.dto.PracticeAnswerRequest;
import com.skillforge.domain.practice.dto.PracticeAnswerResponse;
import com.skillforge.domain.practice.dto.PracticeQuestionResponse;
import com.skillforge.domain.practice.dto.PracticeRecommendationResponse;
import com.skillforge.domain.practice.dto.PracticeStatsOverviewResponse;
import com.skillforge.domain.practice.entity.AptitudeMCQ;
import com.skillforge.domain.practice.entity.MCQOption;
import com.skillforge.domain.practice.entity.PracticeQuestion;
import com.skillforge.domain.practice.entity.PracticeStats;
import com.skillforge.domain.practice.entity.ProgrammingMCQ;
import com.skillforge.domain.practice.entity.TopicMastery;
import com.skillforge.domain.practice.entity.UserAnswer;
import com.skillforge.domain.practice.repository.MCQOptionRepository;
import com.skillforge.domain.practice.repository.PracticeQuestionRepository;
import com.skillforge.domain.practice.repository.PracticeStatsRepository;
import com.skillforge.domain.practice.repository.TopicMasteryRepository;
import com.skillforge.domain.practice.repository.UserAnswerRepository;
import com.skillforge.domain.user.entity.User;
import com.skillforge.domain.user.repository.UserRepository;
import com.skillforge.exception.BadRequestException;
import com.skillforge.exception.ResourceNotFoundException;

@Service
public class PracticeService {

    private final PracticeQuestionRepository practiceQuestionRepository;
    private final MCQOptionRepository mcqOptionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final PracticeStatsRepository practiceStatsRepository;
    private final TopicMasteryRepository topicMasteryRepository;
    private final UserRepository userRepository;

    public PracticeService(
            PracticeQuestionRepository practiceQuestionRepository,
            MCQOptionRepository mcqOptionRepository,
            UserAnswerRepository userAnswerRepository,
            PracticeStatsRepository practiceStatsRepository,
            TopicMasteryRepository topicMasteryRepository,
            UserRepository userRepository) {
        this.practiceQuestionRepository = practiceQuestionRepository;
        this.mcqOptionRepository = mcqOptionRepository;
        this.userAnswerRepository = userAnswerRepository;
        this.practiceStatsRepository = practiceStatsRepository;
        this.topicMasteryRepository = topicMasteryRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<PracticeQuestionResponse> getQuestions(
            PracticeQuestionType type,
            DifficultyLevel difficulty,
            String topic) {
        return practiceQuestionRepository.findFiltered(type, difficulty, topic).stream()
                .map(PracticeQuestionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PracticeRecommendationResponse getRecommendation(String userEmail, PracticeQuestionType type, String topic) {
        User user = findUserByEmail(userEmail);

        return getRecommendationForUser(user.getId(), type, topic);
    }

    @Transactional
    public PracticeAnswerResponse submitAnswer(String userEmail, PracticeAnswerRequest request) {
        User user = findUserByEmail(userEmail);
        PracticeQuestion question = practiceQuestionRepository.findByIdAndIsActiveTrue(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Practice question not found with id: " + request.getQuestionId()));

        MCQOption selectedOption = null;
        if (question.getQuestionType() != PracticeQuestionType.CODING) {
            if (request.getSelectedOptionId() == null) {
                throw new BadRequestException("selectedOptionId is required for MCQ practice questions");
            }

            selectedOption = mcqOptionRepository.findById(request.getSelectedOptionId())
                    .orElseThrow(() -> new ResourceNotFoundException("MCQ option not found with id: " + request.getSelectedOptionId()));

            if (!selectedOption.getQuestion().getId().equals(question.getId())) {
                throw new BadRequestException("Selected option does not belong to the requested question");
            }
        }

        boolean isCorrect = selectedOption != null && Boolean.TRUE.equals(selectedOption.getIsCorrect());
        Long correctOptionId = question.getOptions().stream()
                .filter(option -> Boolean.TRUE.equals(option.getIsCorrect()))
                .map(MCQOption::getId)
                .findFirst()
                .orElse(null);

        UserAnswer answer = new UserAnswer();
        answer.setUser(user);
        answer.setQuestion(question);
        answer.setSelectedOption(selectedOption);
        answer.setCodingAnswer(request.getCodingAnswer());
        answer.setIsCorrect(isCorrect);
        answer.setTimeTakenSeconds(request.getTimeTakenSeconds());
        userAnswerRepository.save(answer);

        PracticeStats stats = practiceStatsRepository.findByUserIdAndQuestionType(user.getId(), question.getQuestionType())
                .orElseGet(() -> {
                    PracticeStats practiceStats = new PracticeStats();
                    practiceStats.setUser(user);
                    practiceStats.setQuestionType(question.getQuestionType());
                    return practiceStats;
                });

        int updatedAttempted = stats.getTotalAttempted() + 1;
        int updatedCorrect = stats.getTotalCorrect() + (isCorrect ? 1 : 0);
        long updatedTime = stats.getTotalTimeTakenSeconds() + request.getTimeTakenSeconds();

        stats.setTotalAttempted(updatedAttempted);
        stats.setTotalCorrect(updatedCorrect);
        stats.setTotalTimeTakenSeconds(updatedTime);
        stats.setAccuracyRate(percentage(updatedCorrect, updatedAttempted));
        stats.setLastAnsweredAt(LocalDateTime.now());
        practiceStatsRepository.save(stats);

        TopicMastery mastery = topicMasteryRepository.findByUserIdAndTopicIgnoreCaseAndQuestionType(
                        user.getId(),
                        question.getTopic(),
                        question.getQuestionType())
                .orElseGet(() -> {
                    TopicMastery topicMastery = new TopicMastery();
                    topicMastery.setUser(user);
                    topicMastery.setTopic(question.getTopic());
                    topicMastery.setQuestionType(question.getQuestionType());
                    return topicMastery;
                });

        int attemptedCount = mastery.getAttemptedCount() + 1;
        int correctCount = mastery.getCorrectCount() + (isCorrect ? 1 : 0);
        int avgTime = (int) Math.round(((double) (mastery.getAvgTimeTakenSeconds() * mastery.getAttemptedCount()) + request.getTimeTakenSeconds())
                / attemptedCount);

        mastery.setAttemptedCount(attemptedCount);
        mastery.setCorrectCount(correctCount);
        mastery.setAvgTimeTakenSeconds(avgTime);
        mastery.setMasteryScore(percentage(correctCount, attemptedCount));

        String weakTopic = weakestTopic(user.getId(), question.getQuestionType()).orElse(question.getTopic());
        PracticeRecommendationResponse nextRecommendation = getRecommendationForUser(user.getId(), question.getQuestionType(), weakTopic);
        mastery.setRecommendedQuestion(practiceQuestionRepository.findByIdAndIsActiveTrue(nextRecommendation.getQuestion().getId()).orElse(null));
        topicMasteryRepository.save(mastery);

        return new PracticeAnswerResponse(
                question.getId(),
                isCorrect,
                request.getSelectedOptionId(),
                correctOptionId,
                extractExplanation(question),
                stats.getAccuracyRate().doubleValue(),
                weakTopic,
                nextRecommendation);
    }

    @Transactional(readOnly = true)
    public PracticeStatsOverviewResponse getStatsOverview(String userEmail, int days) {
        User user = findUserByEmail(userEmail);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusDays(Math.max(days, 1));

        long attempted = userAnswerRepository.countByUserIdAndCreatedAtBetween(user.getId(), from, now);
        long correct = userAnswerRepository.countByUserIdAndIsCorrectTrueAndCreatedAtBetween(user.getId(), from, now);
        double accuracy = attempted == 0 ? 0.0 : ((double) correct * 100.0) / attempted;
        Double averageTime = userAnswerRepository.averageTimeByUserBetween(user.getId(), from, now);

        return new PracticeStatsOverviewResponse(
                attempted,
                correct,
                accuracy,
                averageTime == null ? 0.0 : averageTime);
    }

    private Optional<String> weakestTopic(Long userId, PracticeQuestionType type) {
        return topicMasteryRepository.findByUserIdOrderByMasteryScoreAsc(userId).stream()
                .filter(tm -> type == null || tm.getQuestionType() == type)
                .map(TopicMastery::getTopic)
                .findFirst();
    }

    private PracticeRecommendationResponse getRecommendationForUser(Long userId, PracticeQuestionType type, String topic) {
        String resolvedTopic = topic;
        if (resolvedTopic == null || resolvedTopic.isBlank()) {
            resolvedTopic = weakestTopic(userId, type).orElse(null);
        }

        List<PracticeQuestion> recommendation = practiceQuestionRepository.findRecommendedUnattempted(
                userId,
                type,
                resolvedTopic,
                PageRequest.of(0, 1));

        String strategy = "UNATTEMPTED_LOW_SUCCESS_RATE";
        String reason = resolvedTopic == null || resolvedTopic.isBlank()
                ? "Prioritizes lower global success-rate questions to build mastery progressively."
                : "Prioritizes your weakest topic first, then selects an unattempted question with lower global success rate.";

        if (recommendation.isEmpty() && resolvedTopic != null && !resolvedTopic.isBlank()) {
            recommendation = practiceQuestionRepository.findRecommendedUnattempted(
                    userId,
                    type,
                    null,
                    PageRequest.of(0, 1));
            strategy = "WEAK_TOPIC_EXHAUSTED_UNATTEMPTED";
            reason = "Your weakest topic has no remaining unattempted questions, so the next unattempted question of the same practice type is recommended.";
        }

        if (recommendation.isEmpty()) {
            recommendation = practiceQuestionRepository.findFallbackRecommendation(
                    type,
                    resolvedTopic,
                    PageRequest.of(0, 1));
            strategy = "FALLBACK_LOW_SUCCESS_RATE";
            reason = "All matching questions were attempted; picking the next best challenge by success rate and solve time.";
        }

        if (recommendation.isEmpty() && resolvedTopic != null && !resolvedTopic.isBlank()) {
            recommendation = practiceQuestionRepository.findFallbackRecommendation(
                    type,
                    null,
                    PageRequest.of(0, 1));
            strategy = "GLOBAL_FALLBACK_LOW_SUCCESS_RATE";
            reason = "Your weakest topic is exhausted, so the recommendation widens to the same practice type and picks the next best challenge by success rate and solve time.";
        }

        if (recommendation.isEmpty()) {
            throw new ResourceNotFoundException("No practice questions available for the selected filters");
        }

        return PracticeRecommendationResponse.of(strategy, reason, PracticeQuestionResponse.from(recommendation.get(0)));
    }

    private BigDecimal percentage(int numerator, int denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private String extractExplanation(PracticeQuestion question) {
        ProgrammingMCQ programmingMCQ = question.getProgrammingMCQ();
        if (programmingMCQ != null && programmingMCQ.getExplanation() != null) {
            return programmingMCQ.getExplanation();
        }

        AptitudeMCQ aptitudeMCQ = question.getAptitudeMCQ();
        if (aptitudeMCQ != null && aptitudeMCQ.getExplanation() != null) {
            return aptitudeMCQ.getExplanation();
        }

        return "Review the topic and try a similar question next.";
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
