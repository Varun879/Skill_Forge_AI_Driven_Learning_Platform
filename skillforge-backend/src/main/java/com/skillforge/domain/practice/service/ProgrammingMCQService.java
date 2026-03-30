package com.skillforge.domain.practice.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillforge.common.enums.DifficultyLevel;
import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.domain.practice.dto.PracticeAnswerRequest;
import com.skillforge.domain.practice.dto.PracticeAnswerResponse;
import com.skillforge.domain.practice.dto.PracticeQuestionResponse;
import com.skillforge.domain.practice.dto.PracticeRecommendationResponse;
import com.skillforge.domain.practice.dto.ProgrammingMCQAnswerRequest;
import com.skillforge.domain.practice.dto.ProgrammingMCQAnswerResponse;
import com.skillforge.domain.practice.dto.ProgrammingMCQAttemptResponse;
import com.skillforge.domain.practice.entity.MCQAttempt;
import com.skillforge.domain.practice.entity.PracticeQuestion;
import com.skillforge.domain.practice.recommendation.service.CategoryAvailabilityService;
import com.skillforge.domain.practice.recommendation.service.UserPerformanceAnalyzer;
import com.skillforge.domain.practice.repository.MCQAttemptRepository;
import com.skillforge.domain.practice.repository.PracticeQuestionRepository;
import com.skillforge.domain.user.entity.User;
import com.skillforge.domain.user.repository.UserRepository;
import com.skillforge.exception.BadRequestException;
import com.skillforge.exception.ResourceNotFoundException;

@Service
public class ProgrammingMCQService {

        private static final Logger log = LoggerFactory.getLogger(ProgrammingMCQService.class);

    private final PracticeQuestionRepository practiceQuestionRepository;
    private final PracticeService practiceService;
    private final MCQAttemptRepository mcqAttemptRepository;
    private final UserRepository userRepository;
        private final CategoryAvailabilityService categoryAvailabilityService;
        private final UserPerformanceAnalyzer userPerformanceAnalyzer;

    public ProgrammingMCQService(
            PracticeQuestionRepository practiceQuestionRepository,
            PracticeService practiceService,
            MCQAttemptRepository mcqAttemptRepository,
                        UserRepository userRepository,
                        CategoryAvailabilityService categoryAvailabilityService,
                        UserPerformanceAnalyzer userPerformanceAnalyzer) {
        this.practiceQuestionRepository = practiceQuestionRepository;
        this.practiceService = practiceService;
        this.mcqAttemptRepository = mcqAttemptRepository;
        this.userRepository = userRepository;
                this.categoryAvailabilityService = categoryAvailabilityService;
                this.userPerformanceAnalyzer = userPerformanceAnalyzer;
    }

        @Transactional
    public List<PracticeQuestionResponse> getProgrammingMcqs(
            DifficultyLevel difficulty,
            String topic,
            String tag,
            int limit) {
                categoryAvailabilityService.ensureCategoriesPopulated(PracticeQuestionType.PROGRAMMING_MCQ);

        int resolvedLimit = Math.min(Math.max(limit, 1), 50);
        return practiceQuestionRepository.findProgrammingMcqQuestions(
                        difficulty,
                        topic,
                        tag,
                        PageRequest.of(0, resolvedLimit))
                .stream()
                .map(PracticeQuestionResponse::from)
                .toList();
    }

    @Transactional
    public ProgrammingMCQAnswerResponse answerProgrammingMcq(
            String userEmail,
            ProgrammingMCQAnswerRequest request) {
        log.info("Programming MCQ answer submission received user={}, questionId={}", userEmail, request.getQuestionId());

        PracticeQuestion question = practiceQuestionRepository.findByIdAndIsActiveTrue(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Programming MCQ not found with id: " + request.getQuestionId()));

        if (question.getQuestionType() != PracticeQuestionType.PROGRAMMING_MCQ) {
            log.warn("Programming MCQ submission rejected user={}, questionId={}, actualType={}",
                    userEmail, request.getQuestionId(), question.getQuestionType());
            throw new BadRequestException("questionId does not belong to a programming MCQ");
        }

        PracticeAnswerRequest answerRequest = new PracticeAnswerRequest();
        answerRequest.setQuestionId(request.getQuestionId());
        answerRequest.setSelectedOptionId(request.getSelectedOptionId());
        answerRequest.setTimeTakenSeconds(request.getTimeTakenSeconds());

        PracticeAnswerResponse answerResponse = practiceService.submitAnswer(userEmail, answerRequest);
        log.info("Programming MCQ answer processed user={}, questionId={}, correct={}",
                userEmail, answerResponse.getQuestionId(), answerResponse.isCorrect());

        User user = findUserByEmail(userEmail);
        userPerformanceAnalyzer.recordAnswer(
                user.getId(),
                question.getTopic(),
                PracticeQuestionType.PROGRAMMING_MCQ,
                answerResponse.isCorrect(),
                request.getTimeTakenSeconds(),
                user);

        List<MCQAttempt> recentAttempts = mcqAttemptRepository.findLatestProgrammingAttempts(user.getId(), PageRequest.of(0, 10));
        List<ProgrammingMCQAttemptResponse> attemptHistory = recentAttempts.stream()
                .map(ProgrammingMCQAttemptResponse::from)
                .toList();

        long totalAttempts = mcqAttemptRepository.countProgrammingAttempts(user.getId());
        long totalCorrectAttempts = mcqAttemptRepository.countCorrectProgrammingAttempts(user.getId());
        double accuracyRate = totalAttempts == 0
                ? 0.0
                : ((double) totalCorrectAttempts * 100.0) / totalAttempts;
        Double averageTimeTakenSeconds = mcqAttemptRepository.averageProgrammingAttemptTime(user.getId());

        return new ProgrammingMCQAnswerResponse(
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
    public PracticeRecommendationResponse getNextProgrammingMcq(String userEmail, String topic) {
        return practiceService.getRecommendation(userEmail, PracticeQuestionType.PROGRAMMING_MCQ, topic);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
