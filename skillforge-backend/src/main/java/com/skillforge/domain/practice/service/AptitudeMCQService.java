package com.skillforge.domain.practice.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillforge.common.enums.DifficultyLevel;
import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.domain.practice.dto.AptitudeAnswerRequest;
import com.skillforge.domain.practice.dto.AptitudeAnswerResponse;
import com.skillforge.domain.practice.dto.AptitudeAttemptResponse;
import com.skillforge.domain.practice.dto.PracticeAnswerRequest;
import com.skillforge.domain.practice.dto.PracticeAnswerResponse;
import com.skillforge.domain.practice.dto.PracticeQuestionResponse;
import com.skillforge.domain.practice.dto.PracticeRecommendationResponse;
import com.skillforge.domain.practice.entity.AptitudeAttempt;
import com.skillforge.domain.practice.entity.PracticeQuestion;
import com.skillforge.domain.practice.recommendation.service.CategoryAvailabilityService;
import com.skillforge.domain.practice.recommendation.service.UserPerformanceAnalyzer;
import com.skillforge.domain.practice.repository.AptitudeAttemptRepository;
import com.skillforge.domain.practice.repository.PracticeQuestionRepository;
import com.skillforge.domain.user.entity.User;
import com.skillforge.domain.user.repository.UserRepository;
import com.skillforge.exception.BadRequestException;
import com.skillforge.exception.ResourceNotFoundException;

@Service
public class AptitudeMCQService {

        private static final Logger log = LoggerFactory.getLogger(AptitudeMCQService.class);

    private final PracticeQuestionRepository practiceQuestionRepository;
    private final PracticeService practiceService;
    private final AptitudeAttemptRepository aptitudeAttemptRepository;
    private final UserRepository userRepository;
        private final CategoryAvailabilityService categoryAvailabilityService;
        private final UserPerformanceAnalyzer userPerformanceAnalyzer;

    public AptitudeMCQService(
            PracticeQuestionRepository practiceQuestionRepository,
            PracticeService practiceService,
            AptitudeAttemptRepository aptitudeAttemptRepository,
                        UserRepository userRepository,
                        CategoryAvailabilityService categoryAvailabilityService,
                        UserPerformanceAnalyzer userPerformanceAnalyzer) {
        this.practiceQuestionRepository = practiceQuestionRepository;
        this.practiceService = practiceService;
        this.aptitudeAttemptRepository = aptitudeAttemptRepository;
        this.userRepository = userRepository;
                this.categoryAvailabilityService = categoryAvailabilityService;
                this.userPerformanceAnalyzer = userPerformanceAnalyzer;
    }

        @Transactional
    public List<PracticeQuestionResponse> getAptitudeQuestions(
            DifficultyLevel difficulty,
            String topic,
            String tag,
            int limit) {
                categoryAvailabilityService.ensureCategoriesPopulated(PracticeQuestionType.APTITUDE_MCQ);

        int resolvedLimit = Math.min(Math.max(limit, 1), 50);
        return practiceQuestionRepository.findAptitudeQuestions(
                        difficulty,
                        topic,
                        tag,
                        PageRequest.of(0, resolvedLimit))
                .stream()
                .map(PracticeQuestionResponse::from)
                .toList();
    }

    @Transactional
    public AptitudeAnswerResponse answerAptitudeQuestion(
            String userEmail,
            AptitudeAnswerRequest request) {
        log.info("Aptitude MCQ answer submission received user={}, questionId={}", userEmail, request.getQuestionId());

        PracticeQuestion question = practiceQuestionRepository.findByIdAndIsActiveTrue(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Aptitude MCQ not found with id: " + request.getQuestionId()));

        if (question.getQuestionType() != PracticeQuestionType.APTITUDE_MCQ) {
            log.warn("Aptitude MCQ submission rejected user={}, questionId={}, actualType={}",
                    userEmail, request.getQuestionId(), question.getQuestionType());
            throw new BadRequestException("questionId does not belong to an aptitude MCQ");
        }

        PracticeAnswerRequest answerRequest = new PracticeAnswerRequest();
        answerRequest.setQuestionId(request.getQuestionId());
        answerRequest.setSelectedOptionId(request.getSelectedOptionId());
        answerRequest.setTimeTakenSeconds(request.getTimeTakenSeconds());

        PracticeAnswerResponse answerResponse = practiceService.submitAnswer(userEmail, answerRequest);
        log.info("Aptitude MCQ answer processed user={}, questionId={}, correct={}",
                userEmail, answerResponse.getQuestionId(), answerResponse.isCorrect());

        User user = findUserByEmail(userEmail);
        userPerformanceAnalyzer.recordAnswer(
                user.getId(),
                question.getTopic(),
                PracticeQuestionType.APTITUDE_MCQ,
                answerResponse.isCorrect(),
                request.getTimeTakenSeconds(),
                user);

        List<AptitudeAttempt> recentAttempts = aptitudeAttemptRepository.findLatestAttempts(user.getId(), PageRequest.of(0, 10));
        List<AptitudeAttemptResponse> attemptHistory = recentAttempts.stream()
                .map(AptitudeAttemptResponse::from)
                .toList();

        long totalAttempts = aptitudeAttemptRepository.countAttempts(user.getId());
        long totalCorrectAttempts = aptitudeAttemptRepository.countCorrectAttempts(user.getId());
        double accuracyRate = totalAttempts == 0
                ? 0.0
                : ((double) totalCorrectAttempts * 100.0) / totalAttempts;
        Double averageTimeTakenSeconds = aptitudeAttemptRepository.averageAttemptTime(user.getId());

        return new AptitudeAnswerResponse(
                answerResponse.getQuestionId(),
                answerResponse.isCorrect(),
                answerResponse.getSelectedOptionId(),
                answerResponse.getCorrectOptionId(),
                answerResponse.getExplanation(),
                request.getTimeTakenSeconds(),
                accuracyRate,
                averageTimeTakenSeconds == null ? 0.0 : averageTimeTakenSeconds,
                attemptHistory,
                answerResponse.getNextRecommendation());
    }

    @Transactional(readOnly = true)
    public PracticeRecommendationResponse getNextAptitudeQuestion(String userEmail, String topic) {
        return practiceService.getRecommendation(userEmail, PracticeQuestionType.APTITUDE_MCQ, topic);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
