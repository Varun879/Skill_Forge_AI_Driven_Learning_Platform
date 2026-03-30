package com.skillforge.domain.courseexam.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillforge.common.enums.Role;
import com.skillforge.domain.certificate.entity.Certificate;
import com.skillforge.domain.certificate.service.CertificateService;
import com.skillforge.domain.course.dto.CourseModuleProgressResponse;
import com.skillforge.domain.course.service.CourseModuleService;
import com.skillforge.domain.courseexam.dto.CourseExamAttemptResultResponse;
import com.skillforge.domain.courseexam.dto.StartCourseExamResponse;
import com.skillforge.domain.courseexam.dto.SubmitCourseExamRequest;
import com.skillforge.domain.courseexam.entity.CourseExam;
import com.skillforge.domain.courseexam.entity.CourseExamAttempt;
import com.skillforge.domain.courseexam.entity.CourseExamAttemptAnswer;
import com.skillforge.domain.courseexam.entity.CourseExamAttemptStatus;
import com.skillforge.domain.courseexam.entity.CourseExamQuestion;
import com.skillforge.domain.courseexam.repository.CourseExamAttemptAnswerRepository;
import com.skillforge.domain.courseexam.repository.CourseExamAttemptRepository;
import com.skillforge.domain.courseexam.repository.CourseExamRepository;
import com.skillforge.domain.practice.entity.MCQOption;
import com.skillforge.domain.practice.repository.MCQOptionRepository;
import com.skillforge.domain.user.entity.User;
import com.skillforge.domain.user.repository.UserRepository;
import com.skillforge.exception.BadRequestException;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.exception.UnauthorizedException;

@Service
public class CourseExamAttemptService {

    private final CourseExamRepository courseExamRepository;
    private final CourseExamAttemptRepository attemptRepository;
    private final CourseExamAttemptAnswerRepository answerRepository;
    private final MCQOptionRepository optionRepository;
    private final UserRepository userRepository;
    private final CourseModuleService courseModuleService;
    private final CertificateService certificateService;

    @Value("${course.exam.pass-percent:60}")
    private int passPercent;

    public CourseExamAttemptService(CourseExamRepository courseExamRepository,
                                    CourseExamAttemptRepository attemptRepository,
                                    CourseExamAttemptAnswerRepository answerRepository,
                                    MCQOptionRepository optionRepository,
                                    UserRepository userRepository,
                                    CourseModuleService courseModuleService,
                                    CertificateService certificateService) {
        this.courseExamRepository = courseExamRepository;
        this.attemptRepository = attemptRepository;
        this.answerRepository = answerRepository;
        this.optionRepository = optionRepository;
        this.userRepository = userRepository;
        this.courseModuleService = courseModuleService;
        this.certificateService = certificateService;
    }

    @Transactional
    public StartCourseExamResponse startExam(Long courseId, Long examId, String learnerEmail) {
        User learner = getLearner(learnerEmail);
        CourseExam exam = getPublishedCourseExam(courseId, examId);

        CourseModuleProgressResponse progress = courseModuleService.getProgress(courseId, learnerEmail);
        if (!progress.isAllModulesCompleted()) {
            throw new BadRequestException("Complete all course modules before taking final exam");
        }

        CourseExamAttempt attempt = new CourseExamAttempt();
        attempt.setCourseExam(exam);
        attempt.setLearner(learner);
        attempt.setStatus(CourseExamAttemptStatus.STARTED);
        attempt = attemptRepository.save(attempt);

        List<StartCourseExamResponse.ExamQuestionItem> questions = exam.getQuestions().stream()
                .sorted(java.util.Comparator.comparing(CourseExamQuestion::getQuestionOrder))
                .map(q -> new StartCourseExamResponse.ExamQuestionItem(
                        q.getQuestion().getId(),
                        q.getQuestion().getTitle(),
                        q.getQuestion().getPrompt(),
                        q.getQuestion().getTopic(),
                        q.getQuestion().getDifficultyLevel().name(),
                        q.getQuestionOrder(),
                        optionRepository.findByQuestionIdOrderByDisplayOrderAsc(q.getQuestion().getId())
                                .stream()
                                .map(opt -> new StartCourseExamResponse.OptionItem(opt.getId(), opt.getOptionText()))
                                .toList()
                ))
                .toList();

        return new StartCourseExamResponse(
                attempt.getId(),
                exam.getId(),
                exam.getTitle(),
                exam.getDurationMinutes(),
                attempt.getStartedAt(),
                questions
        );
    }

    @Transactional
    public CourseExamAttemptResultResponse submitExam(Long courseId,
                                                      Long examId,
                                                      SubmitCourseExamRequest request,
                                                      String learnerEmail) {
        User learner = getLearner(learnerEmail);
        CourseExam exam = getPublishedCourseExam(courseId, examId);
        CourseExamAttempt attempt = attemptRepository.findById(request.getAttemptId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam attempt not found"));

        if (!attempt.getCourseExam().getId().equals(exam.getId()) || !attempt.getLearner().getId().equals(learner.getId())) {
            throw new UnauthorizedException("You cannot submit this exam attempt");
        }

        if (attempt.getStatus() != CourseExamAttemptStatus.STARTED) {
            return buildResult(attempt, false);
        }

        answerRepository.deleteByAttemptId(attempt.getId());

        Map<Long, Long> selectedByQuestionId = new HashMap<>();
        for (SubmitCourseExamRequest.AnswerItem answer : request.getAnswers()) {
            selectedByQuestionId.put(answer.getQuestionId(), answer.getSelectedOptionId());
        }

        int total = exam.getQuestions().size();
        int correct = 0;

        for (CourseExamQuestion examQuestion : exam.getQuestions()) {
            Long questionId = examQuestion.getQuestion().getId();
            Long selectedOptionId = selectedByQuestionId.get(questionId);

            CourseExamAttemptAnswer answer = new CourseExamAttemptAnswer();
            answer.setAttempt(attempt);
            answer.setQuestion(examQuestion.getQuestion());

            boolean isCorrect = false;
            if (selectedOptionId != null) {
                MCQOption option = optionRepository.findById(selectedOptionId).orElse(null);
                if (option != null && option.getQuestion().getId().equals(questionId)) {
                    answer.setSelectedOption(option);
                    isCorrect = Boolean.TRUE.equals(option.getIsCorrect());
                }
            }

            answer.setIsCorrect(isCorrect);
            if (isCorrect) {
                correct++;
            }
            answerRepository.save(answer);
        }

        BigDecimal score = total == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf((double) correct * 100.0 / (double) total).setScale(2, RoundingMode.HALF_UP);

        boolean passed = score.compareTo(BigDecimal.valueOf(passPercent)) >= 0;
        attempt.setScore(score);
        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setStatus(passed ? CourseExamAttemptStatus.PASSED : CourseExamAttemptStatus.FAILED);

        boolean certificateIssued = false;
        if (passed) {
            Certificate certificate = certificateService.issueCourseCertificate(learner, exam.getCourse(), score);
            certificateIssued = certificate != null;
        }

        attemptRepository.save(attempt);

        return buildResult(attempt, certificateIssued);
    }

    private CourseExamAttemptResultResponse buildResult(CourseExamAttempt attempt, boolean certificateIssued) {
        CourseExam exam = attempt.getCourseExam();
        int totalQuestions = exam.getQuestions().size();
        int correctAnswers = 0;
        if (attempt.getScore() != null && totalQuestions > 0) {
            correctAnswers = (int) Math.round(attempt.getScore().doubleValue() * totalQuestions / 100.0);
        }

        return new CourseExamAttemptResultResponse(
                attempt.getId(),
                exam.getId(),
                attempt.getScore(),
                totalQuestions,
                correctAnswers,
                attempt.getStatus() == CourseExamAttemptStatus.PASSED,
                attempt.getStatus().name(),
                certificateIssued
        );
    }

    private CourseExam getPublishedCourseExam(Long courseId, Long examId) {
        CourseExam exam = courseExamRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Course exam not found"));

        if (!exam.getCourse().getId().equals(courseId) || !Boolean.TRUE.equals(exam.getIsPublished())) {
            throw new UnauthorizedException("Exam is not available for this course");
        }
        return exam;
    }

    private User getLearner(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() != Role.LEARNER) {
            throw new UnauthorizedException("Only learners can take course final exams");
        }
        return user;
    }
}
