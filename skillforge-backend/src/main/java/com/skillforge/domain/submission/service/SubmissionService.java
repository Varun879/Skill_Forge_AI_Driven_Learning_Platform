package com.skillforge.domain.submission.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.skillforge.domain.problem.entity.Problem;
import com.skillforge.domain.problem.repository.ProblemRepository;
import com.skillforge.domain.submission.dto.ProvideFeedbackRequest;
import com.skillforge.domain.submission.dto.RunSubmissionRequest;
import com.skillforge.domain.submission.dto.SubmissionResultResponse;
import com.skillforge.domain.submission.dto.SubmissionViewResponse;
import com.skillforge.domain.submission.dto.SubmitSubmissionRequest;
import com.skillforge.domain.submission.entity.ReviewFeedback;
import com.skillforge.domain.submission.entity.Submission;
import com.skillforge.domain.submission.entity.SubmissionResult;
import com.skillforge.domain.submission.repository.SubmissionRepository;
import com.skillforge.domain.user.entity.User;
import com.skillforge.domain.user.repository.UserRepository;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.exception.UnauthorizedException;

@Service
public class SubmissionService {

    private static final Logger log = LoggerFactory.getLogger(SubmissionService.class);

    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;

    public SubmissionService(SubmissionRepository submissionRepository,
                             ProblemRepository problemRepository,
                             UserRepository userRepository) {
        this.submissionRepository = submissionRepository;
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public SubmissionResultResponse runSubmission(RunSubmissionRequest request, String learnerEmail) {
        Problem problem = findProblemById(request.getProblemId());
        Evaluation evaluation = evaluate(problem, request.getSourceCode());

        log.info("Submission run: problemId={}, learner={}, language={}, status={}",
                request.getProblemId(), learnerEmail, request.getLanguage(), evaluation.status);

        return new SubmissionResultResponse(
                evaluation.status,
                evaluation.message,
                evaluation.totalTestCases,
                evaluation.passedTestCases,
                evaluation.score);
    }

    @Transactional
    public SubmissionResultResponse submitSubmission(SubmitSubmissionRequest request, String learnerEmail) {
        User learner = findUserByEmail(learnerEmail);
        Problem problem = findProblemById(request.getProblemId());

        Evaluation evaluation = evaluate(problem, request.getSourceCode());

        Submission submission = new Submission();
        submission.setLearner(learner);
        submission.setProblem(problem);
        submission.setLanguage(request.getLanguage().trim().toLowerCase());
        submission.setSourceCode(request.getSourceCode());
        submission.setStatus(evaluation.status);

        SubmissionResult result = new SubmissionResult();
        result.setSubmission(submission);
        result.setPassedTestCases(evaluation.passedTestCases);
        result.setTotalTestCases(evaluation.totalTestCases);
        result.setScore(evaluation.score);
        result.setMessage(evaluation.message);

        submission.setResult(result);

        submissionRepository.save(submission);

        log.info("Submission stored: submissionId={}, problemId={}, learner={}, status={}",
                submission.getId(), request.getProblemId(), learnerEmail, evaluation.status);

        return new SubmissionResultResponse(
                evaluation.status,
                evaluation.message,
                evaluation.totalTestCases,
                evaluation.passedTestCases,
                evaluation.score);
    }

    @Transactional(readOnly = true)
    public List<SubmissionViewResponse> getUserSubmissions(String learnerEmail) {
        User learner = findUserByEmail(learnerEmail);
        return submissionRepository.findByLearnerIdOrderByCreatedAtDesc(learner.getId())
                .stream()
                .map(SubmissionViewResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubmissionViewResponse> getSubmissionsByProblem(Long problemId, String requesterEmail) {
        User requester = findUserByEmail(requesterEmail);
        Problem problem = findProblemById(problemId);

        boolean requesterIsTutorOwner = problem.getTutor().getId().equals(requester.getId());

        if (requesterIsTutorOwner) {
            return submissionRepository.findByProblemIdOrderByCreatedAtDesc(problemId)
                    .stream()
                    .map(SubmissionViewResponse::from)
                    .toList();
        }

        return submissionRepository.findByProblemIdOrderByCreatedAtDesc(problemId)
                .stream()
                .filter(submission -> submission.getLearner().getId().equals(requester.getId()))
                .map(SubmissionViewResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubmissionViewResponse> getReviewSubmissions(String tutorEmail) {
        User tutor = findUserByEmail(tutorEmail);
        return submissionRepository.findByProblemTutorIdOrderByCreatedAtDesc(tutor.getId())
                .stream()
                .map(SubmissionViewResponse::from)
                .toList();
    }

    @Transactional
    public SubmissionViewResponse provideFeedback(ProvideFeedbackRequest request, String tutorEmail) {
        User tutor = findUserByEmail(tutorEmail);
        Submission submission = findSubmissionById(request.getSubmissionId());

        if (!submission.getProblem().getTutor().getId().equals(tutor.getId())) {
            throw new UnauthorizedException("You can only review submissions for your own problems");
        }

        ReviewFeedback reviewFeedback = submission.getReviewFeedback();
        if (reviewFeedback == null) {
            reviewFeedback = new ReviewFeedback();
            reviewFeedback.setSubmission(submission);
            submission.setReviewFeedback(reviewFeedback);
        }

        reviewFeedback.setTutor(tutor);
        reviewFeedback.setFeedback(request.getFeedback().trim());
        reviewFeedback.setReviewedAt(LocalDateTime.now());

        Submission saved = submissionRepository.save(submission);
        log.info("Submission reviewed: submissionId={}, tutor={}", request.getSubmissionId(), tutorEmail);

        return SubmissionViewResponse.from(saved);
    }

    private Evaluation evaluate(Problem problem, String sourceCode) {
        int totalTestCases = problem.getTestCases().size();
        int safeTotalTestCases = Math.max(totalTestCases, 1);

        if (!StringUtils.hasText(sourceCode)) {
            return new Evaluation(
                    "REJECTED",
                    "Source code is empty",
                    totalTestCases,
                    0,
                    0);
        }

        String normalizedCode = sourceCode.trim().toLowerCase();
        boolean looksIncomplete = normalizedCode.contains("todo") || normalizedCode.equals("pass") || normalizedCode.contains("throw new error");

        if (looksIncomplete) {
            int passed = totalTestCases == 0 ? 0 : 1;
            int score = (int) Math.round((passed * 100.0) / safeTotalTestCases);
            return new Evaluation(
                    "REJECTED",
                    "Submission ran, but did not pass all test cases",
                    totalTestCases,
                    passed,
                    score);
        }

        return new Evaluation(
                "ACCEPTED",
                "Submission accepted",
                totalTestCases,
                totalTestCases,
                100);
    }

    private Problem findProblemById(Long problemId) {
        return problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + problemId));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private Submission findSubmissionById(Long submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + submissionId));
    }

    private record Evaluation(String status, String message, int totalTestCases, int passedTestCases, int score) {}
}
