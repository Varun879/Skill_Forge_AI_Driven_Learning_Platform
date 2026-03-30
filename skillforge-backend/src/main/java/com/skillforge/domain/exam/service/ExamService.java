package com.skillforge.domain.exam.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.domain.exam.dto.ExamResultResponse;
import com.skillforge.domain.exam.dto.StartExamResponse;
import com.skillforge.domain.exam.dto.SubmitExamRequest;
import com.skillforge.domain.exam.entity.ExamQuestion;
import com.skillforge.domain.exam.entity.ExamSession;
import com.skillforge.domain.exam.entity.ExamStatus;
import com.skillforge.domain.exam.repository.ExamQuestionRepository;
import com.skillforge.domain.exam.repository.ExamSessionRepository;
import com.skillforge.domain.practice.entity.MCQOption;
import com.skillforge.domain.practice.entity.PracticeQuestion;
import com.skillforge.domain.practice.entity.UserTopicPerformance;
import com.skillforge.domain.practice.repository.MCQOptionRepository;
import com.skillforge.domain.practice.repository.PracticeQuestionRepository;
import com.skillforge.domain.practice.repository.UserAnswerRepository;
import com.skillforge.domain.practice.repository.UserTopicPerformanceRepository;
import com.skillforge.domain.user.entity.User;
import com.skillforge.domain.user.repository.UserRepository;
import com.skillforge.exception.BadRequestException;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.exception.UnauthorizedException;

@Service
public class ExamService {

    private static final List<PracticeQuestionType> EXAM_TYPES = List.of(
        PracticeQuestionType.PROGRAMMING_MCQ,
        PracticeQuestionType.APTITUDE_MCQ
    );

    private final ExamSessionRepository examSessionRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final UserRepository userRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final UserTopicPerformanceRepository topicPerformanceRepository;
    private final PracticeQuestionRepository practiceQuestionRepository;
    private final MCQOptionRepository optionRepository;

    @Value("${exam.question-count:20}")
    private int questionCount;

    @Value("${exam.duration-seconds:1800}")
    private int durationSeconds;

    public ExamService(ExamSessionRepository examSessionRepository,
                       ExamQuestionRepository examQuestionRepository,
                       UserRepository userRepository,
                       UserAnswerRepository userAnswerRepository,
                       UserTopicPerformanceRepository topicPerformanceRepository,
                       PracticeQuestionRepository practiceQuestionRepository,
                       MCQOptionRepository optionRepository) {
        this.examSessionRepository = examSessionRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.userRepository = userRepository;
        this.userAnswerRepository = userAnswerRepository;
        this.topicPerformanceRepository = topicPerformanceRepository;
        this.practiceQuestionRepository = practiceQuestionRepository;
        this.optionRepository = optionRepository;
    }

    @Transactional
    public StartExamResponse startExam(String email) {
        User user = getUser(email);
        List<PracticeQuestion> pickedQuestions = pickExamQuestions(user);
        if (pickedQuestions.isEmpty()) {
            throw new BadRequestException("Not enough practiced MCQs to start exam");
        }

        Collections.shuffle(pickedQuestions);
        int maxQuestions = Math.min(Math.max(questionCount, 1), pickedQuestions.size());
        List<PracticeQuestion> selected = pickedQuestions.subList(0, maxQuestions);

        ExamSession session = new ExamSession();
        session.setUser(user);
        session.setDurationSeconds(durationSeconds);
        session.setEndTime(LocalDateTime.now().plusSeconds(durationSeconds));
        session.setStatus(ExamStatus.STARTED);
        session = examSessionRepository.save(session);

        List<StartExamResponse.ExamQuestionItem> payloadQuestions = new ArrayList<>();
        int order = 1;
        for (PracticeQuestion question : selected) {
            ExamQuestion examQuestion = new ExamQuestion();
            examQuestion.setExamSession(session);
            examQuestion.setQuestion(question);
            examQuestion.setQuestionOrder(order);
            examQuestionRepository.save(examQuestion);

            List<StartExamResponse.OptionItem> options = optionRepository.findByQuestionIdOrderByDisplayOrderAsc(question.getId())
                .stream()
                .map(opt -> new StartExamResponse.OptionItem(opt.getId(), opt.getOptionText()))
                .toList();

            payloadQuestions.add(new StartExamResponse.ExamQuestionItem(
                question.getId(),
                question.getTitle(),
                question.getPrompt(),
                question.getTopic(),
                question.getDifficultyLevel().name(),
                order,
                options
            ));
            order++;
        }

        return new StartExamResponse(
            session.getId(),
            session.getStartTime(),
            session.getEndTime(),
            session.getDurationSeconds(),
            payloadQuestions
        );
    }

    @Transactional
    public ExamResultResponse submitExam(SubmitExamRequest request, String email) {
        User user = getUser(email);
        ExamSession session = examSessionRepository.findById(request.getExamSessionId())
            .orElseThrow(() -> new ResourceNotFoundException("Exam session not found"));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You cannot submit another user's exam");
        }
        if (session.getStatus() != ExamStatus.STARTED) {
            return buildResult(session);
        }

        Map<Long, Long> answerMap = new HashMap<>();
        if (request.getAnswers() != null) {
            for (SubmitExamRequest.AnswerItem answer : request.getAnswers()) {
                answerMap.put(answer.getQuestionId(), answer.getSelectedOptionId());
            }
        }

        List<ExamQuestion> questions = examQuestionRepository.findByExamSessionIdOrderByQuestionOrderAsc(session.getId());
        long correct = 0;
        for (ExamQuestion examQuestion : questions) {
            Long selectedOptionId = answerMap.get(examQuestion.getQuestion().getId());
            if (selectedOptionId == null) {
                examQuestion.setIsCorrect(false);
                examQuestion.setAnsweredAt(LocalDateTime.now());
                continue;
            }

            MCQOption selectedOption = optionRepository.findById(selectedOptionId)
                .orElse(null);
            boolean sameQuestion = selectedOption != null
                && selectedOption.getQuestion().getId().equals(examQuestion.getQuestion().getId());
            boolean isCorrect = sameQuestion && Boolean.TRUE.equals(selectedOption.getIsCorrect());

            examQuestion.setSelectedOption(sameQuestion ? selectedOption : null);
            examQuestion.setIsCorrect(isCorrect);
            examQuestion.setAnsweredAt(LocalDateTime.now());
            if (isCorrect) {
                correct++;
            }
        }

        BigDecimal score = questions.isEmpty()
            ? BigDecimal.ZERO
            : BigDecimal.valueOf((double) correct * 100.0 / (double) questions.size())
                .setScale(2, RoundingMode.HALF_UP);

        session.setScore(score);
        session.setSubmittedAt(LocalDateTime.now());
        session.setStatus(LocalDateTime.now().isAfter(session.getEndTime()) ? ExamStatus.AUTO_SUBMITTED : ExamStatus.SUBMITTED);

        return buildResult(session);
    }

    @Transactional(readOnly = true)
    public ExamResultResponse getResult(Long sessionId, String email) {
        User user = getUser(email);
        ExamSession session;
        if (sessionId == null) {
            session = examSessionRepository.findTopByUserIdOrderByStartTimeDesc(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No exam session found"));
        } else {
            session = examSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam session not found"));
        }

        if (!session.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You cannot access another user's exam result");
        }
        return buildResult(session);
    }

    private ExamResultResponse buildResult(ExamSession session) {
        List<ExamQuestion> examQuestions = examQuestionRepository.findByExamSessionIdOrderByQuestionOrderAsc(session.getId());
        long correct = examQuestions.stream().filter(q -> Boolean.TRUE.equals(q.getIsCorrect())).count();
        List<ExamResultResponse.ResultQuestionItem> answerItems = examQuestions.stream()
            .map(q -> new ExamResultResponse.ResultQuestionItem(
                q.getQuestion().getId(),
                q.getSelectedOption() != null ? q.getSelectedOption().getId() : null,
                q.getIsCorrect()
            ))
            .toList();

        return new ExamResultResponse(
            session.getId(),
            session.getStartTime(),
            session.getEndTime(),
            session.getSubmittedAt(),
            session.getStatus().name(),
            session.getScore(),
            examQuestions.size(),
            correct,
            answerItems
        );
    }

    private List<PracticeQuestion> pickExamQuestions(User user) {
        List<UserTopicPerformance> weakTopics = topicPerformanceRepository
            .findByUserIdAndQuestionTypeOrderByAccuracyAsc(user.getId(), PracticeQuestionType.PROGRAMMING_MCQ);
        List<UserTopicPerformance> weakAptitude = topicPerformanceRepository
            .findByUserIdAndQuestionTypeOrderByAccuracyAsc(user.getId(), PracticeQuestionType.APTITUDE_MCQ);

        Set<String> weakTopicNames = new LinkedHashSet<>();
        weakTopics.stream().limit(3).forEach(row -> weakTopicNames.add(row.getCategory()));
        weakAptitude.stream().limit(3).forEach(row -> weakTopicNames.add(row.getCategory()));

        List<Long> practicedIds = userAnswerRepository.findDistinctQuestionIdsByUserIdAndQuestionTypeIn(user.getId(), EXAM_TYPES);

        Map<Long, PracticeQuestion> chosen = new LinkedHashMap<>();
        if (!practicedIds.isEmpty()) {
            List<PracticeQuestion> practiced = practiceQuestionRepository.findByIdInAndQuestionTypeInAndIsActiveTrue(practicedIds, EXAM_TYPES);
            Collections.shuffle(practiced);
            practiced.forEach(q -> chosen.putIfAbsent(q.getId(), q));
        }

        for (String topic : weakTopicNames) {
            List<PracticeQuestion> byTopic = practiceQuestionRepository.findByTopicIgnoreCaseAndQuestionTypeInAndIsActiveTrue(topic, EXAM_TYPES);
            Collections.shuffle(byTopic);
            byTopic.forEach(q -> chosen.putIfAbsent(q.getId(), q));
        }

        if (chosen.size() < questionCount) {
            List<PracticeQuestion> fallback = practiceQuestionRepository.findByQuestionTypeInAndIsActiveTrue(EXAM_TYPES);
            Collections.shuffle(fallback);
            fallback.forEach(q -> chosen.putIfAbsent(q.getId(), q));
        }

        return new ArrayList<>(chosen.values());
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
