package com.skillforge.domain.execution.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillforge.domain.execution.dto.ExecutionEnqueueResponse;
import com.skillforge.domain.execution.dto.ExecutionRequest;
import com.skillforge.domain.execution.dto.ExecutionResultResponse;
import com.skillforge.domain.execution.entity.Execution;
import com.skillforge.domain.execution.entity.ExecutionMode;
import com.skillforge.domain.execution.entity.ExecutionStatus;
import com.skillforge.domain.execution.entity.ExecutionTestResult;
import com.skillforge.domain.execution.repository.ExecutionRepository;
import com.skillforge.domain.problem.entity.Problem;
import com.skillforge.domain.problem.entity.TestCase;
import com.skillforge.domain.problem.repository.ProblemRepository;
import com.skillforge.domain.submission.entity.Submission;
import com.skillforge.domain.submission.entity.SubmissionResult;
import com.skillforge.domain.submission.repository.SubmissionRepository;
import com.skillforge.domain.user.entity.User;
import com.skillforge.domain.user.repository.UserRepository;
import com.skillforge.domain.execution.websocket.ExecutionSocketHandler;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.exception.UnauthorizedException;

@Service
public class ExecutionService {

    private final ExecutionRepository executionRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final ExecutionQueue executionQueue;
    private final ExecutionRateLimiter rateLimiter;
    private final TestCaseRunner testCaseRunner;
    private final ExecutionSocketHandler socketHandler;

    public ExecutionService(
            ExecutionRepository executionRepository,
            ProblemRepository problemRepository,
            UserRepository userRepository,
            SubmissionRepository submissionRepository,
            ExecutionQueue executionQueue,
            ExecutionRateLimiter rateLimiter,
            TestCaseRunner testCaseRunner,
            ExecutionSocketHandler socketHandler) {
        this.executionRepository = executionRepository;
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
        this.executionQueue = executionQueue;
        this.rateLimiter = rateLimiter;
        this.testCaseRunner = testCaseRunner;
        this.socketHandler = socketHandler;
    }

    @Transactional
    public ExecutionEnqueueResponse enqueueRun(ExecutionRequest request, String learnerEmail) {
        User learner = findUser(learnerEmail);
        rateLimiter.assertRunAllowed(learner.getId());
        Execution execution = createExecution(request, learner, ExecutionMode.RUN);
        executionQueue.enqueue(execution.getId());
        socketHandler.publishStatus(execution.getId(), ExecutionStatus.QUEUED.name());
        return new ExecutionEnqueueResponse(execution.getId(), execution.getStatus().name());
    }

    @Transactional
    public ExecutionEnqueueResponse enqueueSubmit(ExecutionRequest request, String learnerEmail) {
        User learner = findUser(learnerEmail);
        rateLimiter.assertSubmitAllowed(learner.getId());
        Execution execution = createExecution(request, learner, ExecutionMode.SUBMIT);
        executionQueue.enqueue(execution.getId());
        socketHandler.publishStatus(execution.getId(), ExecutionStatus.QUEUED.name());
        return new ExecutionEnqueueResponse(execution.getId(), execution.getStatus().name());
    }

    @Transactional(readOnly = true)
    public ExecutionResultResponse getResult(Long executionId, String learnerEmail) {
        Execution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with id: " + executionId));

        if (!execution.getLearner().getEmail().equals(learnerEmail)) {
            throw new UnauthorizedException("You can only access your own execution results");
        }

        return ExecutionResultResponse.from(execution);
    }

    @Transactional
    public void processQueuedExecution(Long executionId) {
        Execution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with id: " + executionId));

        execution.setStatus(ExecutionStatus.RUNNING);
        execution.setStartedAt(LocalDateTime.now());
        executionRepository.save(execution);
        socketHandler.publishStatus(executionId, ExecutionStatus.RUNNING.name());

        List<TestCase> testCases = selectTestCases(execution);
        TestCaseRunner.AggregateExecutionResult aggregate = testCaseRunner.run(
                execution.getLanguage(),
                execution.getSourceCode(),
                execution.getStdinData(),
                testCases,
                chunk -> socketHandler.publishOutput(executionId, chunk));

        execution.setStatus(aggregate.status());
        execution.setStdoutData(aggregate.stdout());
        execution.setStderrData(aggregate.stderr());
        execution.setExecutionTimeMs(aggregate.executionTimeMs());
        execution.setMemoryUsageKb(aggregate.memoryUsageKb());
        execution.setCacheHit(aggregate.cacheHit());
        execution.setCompletedAt(LocalDateTime.now());

        execution.getTestResults().clear();
        for (ExecutionTestResult result : aggregate.caseResults()) {
            result.setExecution(execution);
            execution.getTestResults().add(result);
        }

        if (execution.getMode() == ExecutionMode.SUBMIT) {
            execution.setSubmission(createSubmission(execution, aggregate.passed(), aggregate.total()));
        }

        executionRepository.save(execution);
        socketHandler.publishStatus(executionId, execution.getStatus().name());
    }

    private Execution createExecution(ExecutionRequest request, User learner, ExecutionMode mode) {
        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + request.getProblemId()));

        Execution execution = new Execution();
        execution.setLearner(learner);
        execution.setProblem(problem);
        execution.setMode(mode);
        execution.setLanguage(request.getLanguage().trim().toLowerCase());
        execution.setSourceCode(request.getSourceCode());
        execution.setStdinData(request.getStdin());
        execution.setStatus(ExecutionStatus.QUEUED);
        return executionRepository.save(execution);
    }

    private List<TestCase> selectTestCases(Execution execution) {
        List<TestCase> all = execution.getProblem().getTestCases();
        if (execution.getMode() == ExecutionMode.RUN) {
            List<TestCase> sample = all.stream().filter(TestCase::isSample).toList();
            return sample.isEmpty() ? all : sample;
        }

        List<TestCase> hidden = all.stream().filter(tc -> !tc.isSample()).toList();
        return hidden.isEmpty() ? all : hidden;
    }

    private Submission createSubmission(Execution execution, int passed, int total) {
        Submission submission = new Submission();
        submission.setLearner(execution.getLearner());
        submission.setProblem(execution.getProblem());
        submission.setLanguage(execution.getLanguage());
        submission.setSourceCode(execution.getSourceCode());
        submission.setStatus(execution.getStatus() == ExecutionStatus.SUCCESS ? "ACCEPTED" : "REJECTED");

        SubmissionResult result = new SubmissionResult();
        result.setSubmission(submission);
        result.setPassedTestCases(passed);
        result.setTotalTestCases(total);
        result.setScore(total == 0 ? 0 : (int) Math.round((passed * 100.0) / total));
        result.setMessage(execution.getStatus().name());
        submission.setResult(result);

        return submissionRepository.save(submission);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
