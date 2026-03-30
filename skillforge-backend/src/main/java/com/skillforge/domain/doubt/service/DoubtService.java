package com.skillforge.domain.doubt.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillforge.common.enums.DoubtStatus;
import com.skillforge.common.enums.Role;
import com.skillforge.domain.doubt.dto.AnswerDoubtRequest;
import com.skillforge.domain.doubt.dto.CreateDoubtRequest;
import com.skillforge.domain.doubt.dto.DoubtResponse;
import com.skillforge.domain.doubt.entity.Doubt;
import com.skillforge.domain.doubt.entity.DoubtAnswer;
import com.skillforge.domain.doubt.repository.DoubtRepository;
import com.skillforge.domain.problem.entity.Problem;
import com.skillforge.domain.problem.repository.ProblemRepository;
import com.skillforge.domain.user.entity.User;
import com.skillforge.domain.user.repository.UserRepository;
import com.skillforge.exception.BadRequestException;
import com.skillforge.exception.ResourceNotFoundException;

@Service
public class DoubtService {

    private final DoubtRepository doubtRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;

    public DoubtService(DoubtRepository doubtRepository,
                        ProblemRepository problemRepository,
                        UserRepository userRepository) {
        this.doubtRepository = doubtRepository;
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<DoubtResponse> getDoubts(String requesterEmail, Long problemId, String statusValue) {
        User requester = findUserByEmail(requesterEmail);
        DoubtStatus status = parseStatus(statusValue);

        List<Doubt> doubts = requester.getRole() == Role.TUTOR
                ? findTutorDoubts(requester.getId(), problemId, status)
                : findLearnerDoubts(requester.getId(), problemId, status);

        return doubts.stream().map(DoubtResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public DoubtResponse getDoubt(Long doubtId, String requesterEmail) {
        User requester = findUserByEmail(requesterEmail);
        Doubt doubt = findDoubtById(doubtId);
        verifyAccess(doubt, requester);
        return DoubtResponse.fromWithAnswers(doubt);
    }

    @Transactional
    public DoubtResponse createDoubt(CreateDoubtRequest request, String learnerEmail) {
        User learner = findUserByEmail(learnerEmail);
        Problem problem = findProblemById(request.getProblemId());

        Doubt doubt = new Doubt();
        doubt.setLearner(learner);
        doubt.setProblem(problem);
        doubt.setQuestion(request.getQuestion().trim());
        doubt.setStatus(DoubtStatus.OPEN);

        return DoubtResponse.from(doubtRepository.save(doubt));
    }

    @Transactional
    public DoubtResponse answerDoubt(Long doubtId, AnswerDoubtRequest request, String tutorEmail) {
        User tutor = findUserByEmail(tutorEmail);
        Doubt doubt = findDoubtById(doubtId);

        if (!doubt.getProblem().getTutor().getId().equals(tutor.getId())) {
            throw new AccessDeniedException("You can only answer doubts for your own problems");
        }

        DoubtAnswer answer = new DoubtAnswer();
        answer.setDoubt(doubt);
        answer.setTutor(tutor);
        answer.setAnswer(request.getAnswer().trim());

        doubt.getAnswers().add(answer);
        doubt.setStatus(DoubtStatus.RESOLVED);

        return DoubtResponse.fromWithAnswers(doubtRepository.save(doubt));
    }

    private List<Doubt> findLearnerDoubts(Long learnerId, Long problemId, DoubtStatus status) {
        if (problemId != null && status != null) {
            return doubtRepository.findByLearnerIdAndProblemIdAndStatusOrderByCreatedAtDesc(learnerId, problemId, status);
        }
        if (problemId != null) {
            return doubtRepository.findByLearnerIdAndProblemIdOrderByCreatedAtDesc(learnerId, problemId);
        }
        if (status != null) {
            return doubtRepository.findByLearnerIdAndStatusOrderByCreatedAtDesc(learnerId, status);
        }
        return doubtRepository.findByLearnerIdOrderByCreatedAtDesc(learnerId);
    }

    private List<Doubt> findTutorDoubts(Long tutorId, Long problemId, DoubtStatus status) {
        if (problemId != null && status != null) {
            return doubtRepository.findByProblemTutorIdAndProblemIdAndStatusOrderByCreatedAtDesc(tutorId, problemId, status);
        }
        if (problemId != null) {
            return doubtRepository.findByProblemTutorIdAndProblemIdOrderByCreatedAtDesc(tutorId, problemId);
        }
        if (status != null) {
            return doubtRepository.findByProblemTutorIdAndStatusOrderByCreatedAtDesc(tutorId, status);
        }
        return doubtRepository.findByProblemTutorIdOrderByCreatedAtDesc(tutorId);
    }

    private void verifyAccess(Doubt doubt, User requester) {
        if (requester.getRole() == Role.TUTOR) {
            if (!doubt.getProblem().getTutor().getId().equals(requester.getId())) {
                throw new AccessDeniedException("You can only view doubts for your own problems");
            }
            return;
        }

        if (!doubt.getLearner().getId().equals(requester.getId())) {
            throw new AccessDeniedException("You can only view your own doubts");
        }
    }

    private DoubtStatus parseStatus(String statusValue) {
        if (statusValue == null || statusValue.isBlank()) {
            return null;
        }

        try {
            return DoubtStatus.valueOf(statusValue.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid doubt status: " + statusValue);
        }
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private Problem findProblemById(Long problemId) {
        return problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + problemId));
    }

    private Doubt findDoubtById(Long doubtId) {
        return doubtRepository.findById(doubtId)
                .orElseThrow(() -> new ResourceNotFoundException("Doubt not found with id: " + doubtId));
    }
}