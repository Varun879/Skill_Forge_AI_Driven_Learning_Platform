package com.skillforge.domain.execution.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.skillforge.domain.execution.entity.ExecutionStatus;
import com.skillforge.domain.execution.entity.ExecutionTestResult;
import com.skillforge.domain.problem.entity.TestCase;

@Service
public class TestCaseRunner {

    private final DockerExecutor dockerExecutor;
    private final CompilationCacheService compilationCacheService;

    public TestCaseRunner(DockerExecutor dockerExecutor, CompilationCacheService compilationCacheService) {
        this.dockerExecutor = dockerExecutor;
        this.compilationCacheService = compilationCacheService;
    }

    public AggregateExecutionResult run(
            String language,
            String sourceCode,
            String stdinOverride,
            List<TestCase> testCases,
            Consumer<String> streamOutput) {
        String cacheKey = dockerExecutor.hashSource(language, sourceCode);
        String cachedCompilationError = compilationCacheService.getCompilationError(cacheKey);
        if (cachedCompilationError != null) {
            return compileErrorResult(cachedCompilationError, testCases, true);
        }

        int total = testCases.size();
        int passed = 0;
        long totalTime = 0L;
        long maxMemory = 0L;
        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();
        List<ExecutionTestResult> caseResults = new ArrayList<>();
        ExecutionStatus dominantFailure = null;

        for (TestCase testCase : testCases) {
            String effectiveInput = StringUtils.hasText(stdinOverride) ? stdinOverride : testCase.getInputData();
            DockerExecutor.ExecutionCommandResult result =
                    dockerExecutor.execute(language, sourceCode, effectiveInput, streamOutput);

            totalTime += result.executionTimeMs();
            maxMemory = Math.max(maxMemory, result.memoryUsageKb());
            stdout.append(result.stdout());
            stderr.append(result.stderr());

            String expected = normalize(testCase.getExpectedOutput());
            String actual = normalize(result.stdout());
            boolean matched = result.status() == ExecutionStatus.SUCCESS && expected.equals(actual);

            if (matched) {
                passed++;
            }

            ExecutionTestResult row = new ExecutionTestResult();
            row.setTestCase(testCase);
            row.setSample(testCase.isSample());
            row.setPassed(matched);
            row.setExpectedOutput(testCase.getExpectedOutput());
            row.setActualOutput(result.stdout());
            row.setStdoutData(result.stdout());
            row.setStderrData(result.stderr());
            row.setExecutionTimeMs(result.executionTimeMs());
            row.setMemoryUsageKb(result.memoryUsageKb());
            row.setStatus(matched ? "SUCCESS" : inferCaseStatus(result.status()));
            caseResults.add(row);

            if (!matched) {
                ExecutionStatus thisFailure = mapFailureStatus(result.status());
                dominantFailure = chooseDominantFailure(dominantFailure, thisFailure);
            }

            if (result.status() == ExecutionStatus.COMPILATION_ERROR) {
                compilationCacheService.putCompilationError(cacheKey, result.stderr());
                break;
            }
        }

        ExecutionStatus overall;
        if (total == 0 || passed == total) {
            overall = ExecutionStatus.SUCCESS;
        } else {
            overall = dominantFailure != null ? dominantFailure : ExecutionStatus.FAILED;
        }

        return new AggregateExecutionResult(overall, stdout.toString(), stderr.toString(), totalTime, maxMemory, passed, total, false, caseResults);
    }

    private AggregateExecutionResult compileErrorResult(String stderr, List<TestCase> testCases, boolean cacheHit) {
        List<ExecutionTestResult> results = new ArrayList<>();
        for (TestCase testCase : testCases) {
            ExecutionTestResult row = new ExecutionTestResult();
            row.setTestCase(testCase);
            row.setSample(testCase.isSample());
            row.setPassed(false);
            row.setStatus(ExecutionStatus.COMPILATION_ERROR.name());
            row.setExpectedOutput(testCase.getExpectedOutput());
            row.setActualOutput("");
            row.setStdoutData("");
            row.setStderrData(stderr);
            row.setExecutionTimeMs(0L);
            row.setMemoryUsageKb(0L);
            results.add(row);
        }
        return new AggregateExecutionResult(ExecutionStatus.COMPILATION_ERROR, "", stderr, 0L, 0L, 0, testCases.size(), cacheHit, results);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().replace("\r\n", "\n");
    }

    private ExecutionStatus mapFailureStatus(ExecutionStatus status) {
        if (status == ExecutionStatus.COMPILATION_ERROR
                || status == ExecutionStatus.TLE
                || status == ExecutionStatus.MEMORY_LIMIT_EXCEEDED
                || status == ExecutionStatus.RUNTIME_ERROR) {
            return status;
        }
        return ExecutionStatus.FAILED;
    }

    private ExecutionStatus chooseDominantFailure(ExecutionStatus current, ExecutionStatus next) {
        if (current == null) {
            return next;
        }

        int currentPriority = failurePriority(current);
        int nextPriority = failurePriority(next);
        return nextPriority < currentPriority ? next : current;
    }

    private int failurePriority(ExecutionStatus status) {
        return switch (status) {
            case COMPILATION_ERROR -> 1;
            case TLE -> 2;
            case MEMORY_LIMIT_EXCEEDED -> 3;
            case RUNTIME_ERROR -> 4;
            default -> 5;
        };
    }

    private String inferCaseStatus(ExecutionStatus status) {
        if (status == ExecutionStatus.SUCCESS) {
            return "WRONG_ANSWER";
        }
        return status.name();
    }

    public record AggregateExecutionResult(
            ExecutionStatus status,
            String stdout,
            String stderr,
            long executionTimeMs,
            long memoryUsageKb,
            int passed,
            int total,
            boolean cacheHit,
            List<ExecutionTestResult> caseResults) {}
}
