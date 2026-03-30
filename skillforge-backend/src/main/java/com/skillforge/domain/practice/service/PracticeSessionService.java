package com.skillforge.domain.practice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.common.enums.PracticeSessionType;
import com.skillforge.domain.practice.dto.PracticeAnswerRequest;
import com.skillforge.domain.practice.dto.PracticeAnswerResponse;
import com.skillforge.domain.practice.dto.PracticeSessionHistoryItemResponse;
import com.skillforge.domain.practice.dto.PracticeSessionStartRequest;
import com.skillforge.domain.practice.dto.PracticeSessionStartResponse;
import com.skillforge.domain.practice.dto.PracticeSessionSubmitRequest;
import com.skillforge.domain.practice.dto.PracticeSessionSubmitResponse;
import com.skillforge.domain.practice.dto.SessionQuestionResponse;
import com.skillforge.domain.practice.entity.PracticeQuestion;
import com.skillforge.domain.practice.entity.PracticeSession;
import com.skillforge.domain.practice.entity.SessionQuestion;
import com.skillforge.domain.practice.repository.PracticeQuestionRepository;
import com.skillforge.domain.practice.repository.PracticeSessionRepository;
import com.skillforge.domain.practice.repository.SessionQuestionRepository;
import com.skillforge.domain.user.entity.User;
import com.skillforge.domain.user.repository.UserRepository;
import com.skillforge.exception.BadRequestException;
import com.skillforge.exception.ResourceNotFoundException;

@Service
public class PracticeSessionService {

    private final PracticeSessionRepository practiceSessionRepository;
    private final SessionQuestionRepository sessionQuestionRepository;
    private final PracticeQuestionRepository practiceQuestionRepository;
    private final PracticeService practiceService;
    private final UserRepository userRepository;

    public PracticeSessionService(
            PracticeSessionRepository practiceSessionRepository,
            SessionQuestionRepository sessionQuestionRepository,
            PracticeQuestionRepository practiceQuestionRepository,
            PracticeService practiceService,
            UserRepository userRepository) {
        this.practiceSessionRepository = practiceSessionRepository;
        this.sessionQuestionRepository = sessionQuestionRepository;
        this.practiceQuestionRepository = practiceQuestionRepository;
        this.practiceService = practiceService;
        this.userRepository = userRepository;
    }

    @Transactional
    public PracticeSessionStartResponse startSession(String userEmail, PracticeSessionStartRequest request) {
        User user = findUserByEmail(userEmail);

        PracticeSession session = new PracticeSession();
        session.setUser(user);
        session.setSessionType(request.getSessionType());
        session.setStartedAt(LocalDateTime.now());
        session.setTotalTimeTakenSeconds(0);
        session.setAccuracyRate(BigDecimal.ZERO);

        PracticeSession savedSession = practiceSessionRepository.save(session);
        return PracticeSessionStartResponse.from(savedSession);
    }

    @Transactional
    public PracticeSessionSubmitResponse submitToSession(String userEmail, PracticeSessionSubmitRequest request) {
        User user = findUserByEmail(userEmail);
        PracticeSession session = practiceSessionRepository.findByIdAndUserId(request.getSessionId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Practice session not found with id: " + request.getSessionId()));

        PracticeQuestion question = practiceQuestionRepository.findByIdAndIsActiveTrue(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Practice question not found with id: " + request.getQuestionId()));

        validateQuestionTypeForSession(session.getSessionType(), question.getQuestionType());

        PracticeAnswerRequest answerRequest = new PracticeAnswerRequest();
        answerRequest.setQuestionId(request.getQuestionId());
        answerRequest.setSelectedOptionId(request.getSelectedOptionId());
        answerRequest.setCodingAnswer(request.getCodingAnswer());
        answerRequest.setTimeTakenSeconds(request.getTimeTakenSeconds());

        PracticeAnswerResponse answerResponse = practiceService.submitAnswer(userEmail, answerRequest);

        SessionQuestion sessionQuestion = new SessionQuestion();
        sessionQuestion.setSession(session);
        sessionQuestion.setQuestion(question);
        sessionQuestion.setSelectedOption(question.getQuestionType() == PracticeQuestionType.CODING ? null : question.getOptions().stream()
                .filter(option -> option.getId().equals(request.getSelectedOptionId()))
                .findFirst()
                .orElse(null));
        sessionQuestion.setIsCorrect(answerResponse.isCorrect());
        sessionQuestion.setTimeTakenSeconds(request.getTimeTakenSeconds());
        sessionQuestionRepository.save(sessionQuestion);

        SessionMetrics metrics = computeAndUpdateSessionMetrics(session);

        List<SessionQuestionResponse> recentAttempts = sessionQuestionRepository
                .findTop10BySessionIdOrderByCreatedAtDesc(session.getId())
                .stream()
                .map(SessionQuestionResponse::from)
                .toList();

        return new PracticeSessionSubmitResponse(
                session.getId(),
                question.getId(),
                answerResponse.isCorrect(),
                (int) metrics.totalAttempted,
                metrics.totalTime.intValue(),
                metrics.accuracyRate,
                (int) metrics.topicsCovered,
                recentAttempts);
    }

    @Transactional(readOnly = true)
    public List<PracticeSessionHistoryItemResponse> getSessionHistory(String userEmail) {
        User user = findUserByEmail(userEmail);

        return practiceSessionRepository.findTop10ByUserIdOrderByStartedAtDesc(user.getId())
                .stream()
                .map(this::mapToHistoryItem)
                .toList();
    }

    private PracticeSessionHistoryItemResponse mapToHistoryItem(PracticeSession session) {
        long totalAttempted = sessionQuestionRepository.countBySessionId(session.getId());
        Long totalTime = sessionQuestionRepository.sumTimeTakenBySessionId(session.getId());
        long topicsCovered = sessionQuestionRepository.countDistinctTopicsBySessionId(session.getId());
        List<SessionQuestionResponse> recentAttempts = sessionQuestionRepository
                .findTop10BySessionIdOrderByCreatedAtDesc(session.getId())
                .stream()
                .map(SessionQuestionResponse::from)
                .toList();

        return new PracticeSessionHistoryItemResponse(
                session.getId(),
                session.getSessionType(),
                session.getStartedAt(),
                session.getEndedAt(),
                (int) totalAttempted,
                totalTime == null ? 0 : totalTime.intValue(),
                session.getAccuracyRate() == null ? BigDecimal.ZERO : session.getAccuracyRate(),
                (int) topicsCovered,
                recentAttempts);
    }

    private SessionMetrics computeAndUpdateSessionMetrics(PracticeSession session) {
        long totalAttempted = sessionQuestionRepository.countBySessionId(session.getId());
        long totalCorrect = sessionQuestionRepository.countBySessionIdAndIsCorrectTrue(session.getId());
        Long totalTime = sessionQuestionRepository.sumTimeTakenBySessionId(session.getId());
        long topicsCovered = sessionQuestionRepository.countDistinctTopicsBySessionId(session.getId());

        BigDecimal accuracy = totalAttempted == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(totalCorrect)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalAttempted), 2, RoundingMode.HALF_UP);

        session.setTotalTimeTakenSeconds(totalTime == null ? 0 : totalTime.intValue());
        session.setAccuracyRate(accuracy);
        session.setEndedAt(LocalDateTime.now());
        practiceSessionRepository.save(session);

        return new SessionMetrics(totalAttempted, totalTime == null ? 0L : totalTime, topicsCovered, accuracy);
    }

    private void validateQuestionTypeForSession(PracticeSessionType sessionType, PracticeQuestionType questionType) {
        if (sessionType == PracticeSessionType.MIXED) {
            return;
        }

        if (sessionType == PracticeSessionType.CODING && questionType != PracticeQuestionType.CODING) {
            throw new BadRequestException("Session type CODING only allows CODING questions");
        }

        if (sessionType == PracticeSessionType.PROGRAMMING_MCQ
                && questionType != PracticeQuestionType.PROGRAMMING_MCQ) {
            throw new BadRequestException("Session type PROGRAMMING_MCQ only allows PROGRAMMING_MCQ questions");
        }

        if (sessionType == PracticeSessionType.APTITUDE_MCQ && questionType != PracticeQuestionType.APTITUDE_MCQ) {
            throw new BadRequestException("Session type APTITUDE_MCQ only allows APTITUDE_MCQ questions");
        }
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private record SessionMetrics(long totalAttempted, Long totalTime, long topicsCovered, BigDecimal accuracyRate) {
    }
}
