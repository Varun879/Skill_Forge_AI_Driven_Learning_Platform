package com.skillforge.domain.problem.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.skillforge.common.enums.DifficultyLevel;
import com.skillforge.domain.problem.dto.CreateProblemRequest;
import com.skillforge.domain.problem.dto.ProblemListResponse;
import com.skillforge.domain.problem.dto.ProblemResponse;
import com.skillforge.domain.problem.dto.ProblemTestCaseRequest;
import com.skillforge.domain.problem.dto.SubmissionResponse;
import com.skillforge.domain.problem.dto.SubmitSolutionRequest;
import com.skillforge.domain.problem.dto.UpdateProblemRequest;
import com.skillforge.domain.problem.entity.Problem;
import com.skillforge.domain.problem.entity.ProblemTag;
import com.skillforge.domain.problem.entity.TestCase;
import com.skillforge.domain.problem.repository.ProblemRepository;
import com.skillforge.domain.user.entity.User;
import com.skillforge.domain.user.repository.UserRepository;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.exception.UnauthorizedException;

@Service
public class ProblemService {

    private static final Logger log = LoggerFactory.getLogger(ProblemService.class);

    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;

    public ProblemService(ProblemRepository problemRepository, UserRepository userRepository) {
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProblemResponse createProblem(CreateProblemRequest request, String tutorEmail) {
        User tutor = findUserByEmail(tutorEmail);

        Problem problem = new Problem();
        problem.setTutor(tutor);
        problem.setTitle(request.getTitle());
        problem.setDescription(request.getDescription());
        problem.setDifficultyLevel(request.getDifficultyLevel());
        problem.setConstraintsText(joinLines(request.getConstraints()));

        applyTags(problem, request.getTags());
        applyTestCases(problem, request.getTestCases());

        Problem saved = problemRepository.save(problem);
        log.info("Problem created: id={}, tutor={}", saved.getId(), tutorEmail);

        return ProblemResponse.from(saved);
    }

    @Transactional
    public ProblemResponse updateProblem(Long problemId, UpdateProblemRequest request, String tutorEmail) {
        Problem problem = findProblemById(problemId);
        User tutor = findUserByEmail(tutorEmail);

        assertTutorOwnsProblem(problem, tutor);

        if (StringUtils.hasText(request.getTitle())) {
            problem.setTitle(request.getTitle());
        }
        if (StringUtils.hasText(request.getDescription())) {
            problem.setDescription(request.getDescription());
        }
        if (request.getDifficultyLevel() != null) {
            problem.setDifficultyLevel(request.getDifficultyLevel());
        }
        if (request.getConstraints() != null) {
            problem.setConstraintsText(joinLines(request.getConstraints()));
        }
        if (request.getTags() != null) {
            problem.getTags().clear();
            applyTags(problem, request.getTags());
        }
        if (request.getTestCases() != null && !request.getTestCases().isEmpty()) {
            problem.getTestCases().clear();
            applyTestCases(problem, request.getTestCases());
        }

        log.info("Problem updated: id={}, tutor={}", problemId, tutorEmail);
        return ProblemResponse.from(problem);
    }

    @Transactional
    public void deleteProblem(Long problemId, String tutorEmail) {
        Problem problem = findProblemById(problemId);
        User tutor = findUserByEmail(tutorEmail);

        assertTutorOwnsProblem(problem, tutor);

        problemRepository.delete(problem);
        log.info("Problem deleted: id={}, tutor={}", problemId, tutorEmail);
    }

    @Transactional(readOnly = true)
    public List<ProblemListResponse> getProblems(DifficultyLevel difficultyLevel) {
        List<Problem> problems = difficultyLevel == null
                ? problemRepository.findAllByOrderByCreatedAtDesc()
                : problemRepository.findByDifficultyLevelOrderByCreatedAtDesc(difficultyLevel);

        return problems.stream().map(ProblemListResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProblemResponse getProblemById(Long problemId) {
        return ProblemResponse.from(findProblemById(problemId));
    }

    @Transactional(readOnly = true)
    public SubmissionResponse submitSolution(Long problemId, SubmitSolutionRequest request, String learnerEmail) {
        Problem problem = findProblemById(problemId);

        log.info("Solution submitted: problemId={}, learner={}, language={}",
                problemId, learnerEmail, request.getLanguage());

        return new SubmissionResponse(
                "SUBMITTED",
                "Solution submitted successfully",
                problem.getTestCases().size());
    }

    private void applyTags(Problem problem, List<String> tags) {
        if (tags == null) {
            return;
        }

        tags.stream()
                .filter(StringUtils::hasText)
                .map(tag -> tag.trim().toLowerCase())
                .distinct()
                .map(tag -> new ProblemTag(problem, tag))
                .forEach(problem.getTags()::add);
    }

    private void applyTestCases(Problem problem, List<ProblemTestCaseRequest> testCases) {
        if (testCases == null) {
            return;
        }

        testCases.stream()
                .filter(tc -> StringUtils.hasText(tc.getInput()) && StringUtils.hasText(tc.getOutput()))
                .map(tc -> new TestCase(
                        problem,
                        tc.getInput().trim(),
                        tc.getOutput().trim(),
                        tc.getExplanation(),
                        tc.getSample() == null || tc.getSample()))
                .forEach(problem.getTestCases()::add);
    }

    private String joinLines(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }

        String result = values.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .reduce((first, second) -> first + "\n" + second)
                .orElse(null);

        return StringUtils.hasText(result) ? result : null;
    }

    private Problem findProblemById(Long problemId) {
        return problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Problem not found with id: " + problemId));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + email));
    }

    private void assertTutorOwnsProblem(Problem problem, User tutor) {
        if (!problem.getTutor().getId().equals(tutor.getId())) {
            throw new UnauthorizedException("You are not the owner of this problem");
        }
    }
}
