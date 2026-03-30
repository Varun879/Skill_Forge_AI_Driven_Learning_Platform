package com.skillforge.domain.practice.service;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.domain.practice.dto.PracticeQuestionResponse;
import com.skillforge.domain.practice.dto.TutorCreateAptitudeMcqRequest;
import com.skillforge.domain.practice.dto.TutorCreateCodingProblemRequest;
import com.skillforge.domain.practice.dto.TutorCreateProgrammingMcqRequest;
import com.skillforge.domain.practice.dto.TutorMcqOptionCreateRequest;
import com.skillforge.domain.practice.entity.AptitudeMCQ;
import com.skillforge.domain.practice.entity.MCQOption;
import com.skillforge.domain.practice.entity.PracticeQuestion;
import com.skillforge.domain.practice.entity.ProgrammingMCQ;
import com.skillforge.domain.practice.repository.AptitudeMCQRepository;
import com.skillforge.domain.practice.repository.MCQOptionRepository;
import com.skillforge.domain.practice.repository.PracticeQuestionRepository;
import com.skillforge.domain.practice.repository.ProgrammingMCQRepository;
import com.skillforge.domain.user.repository.UserRepository;
import com.skillforge.exception.BadRequestException;
import com.skillforge.exception.ResourceNotFoundException;

@Service
public class TutorPracticeQuestionService {

    private final PracticeQuestionRepository practiceQuestionRepository;
    private final ProgrammingMCQRepository programmingMCQRepository;
    private final AptitudeMCQRepository aptitudeMCQRepository;
    private final MCQOptionRepository mcqOptionRepository;
    private final UserRepository userRepository;

    public TutorPracticeQuestionService(
            PracticeQuestionRepository practiceQuestionRepository,
            ProgrammingMCQRepository programmingMCQRepository,
            AptitudeMCQRepository aptitudeMCQRepository,
            MCQOptionRepository mcqOptionRepository,
            UserRepository userRepository) {
        this.practiceQuestionRepository = practiceQuestionRepository;
        this.programmingMCQRepository = programmingMCQRepository;
        this.aptitudeMCQRepository = aptitudeMCQRepository;
        this.mcqOptionRepository = mcqOptionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PracticeQuestionResponse createCodingProblem(TutorCreateCodingProblemRequest request, String tutorEmail) {
        ensureTutorExists(tutorEmail);

        PracticeQuestion question = buildBaseQuestion(
                request.getTitle(),
                request.getQuestion(),
                request.getDifficultyLevel(),
                request.getTopic(),
                request.getTags(),
                request.getEstimatedSolveTimeMinutes(),
                PracticeQuestionType.CODING);

        PracticeQuestion saved = practiceQuestionRepository.save(question);
        return PracticeQuestionResponse.from(saved);
    }

    @Transactional
    public PracticeQuestionResponse createProgrammingMcq(TutorCreateProgrammingMcqRequest request, String tutorEmail) {
        ensureTutorExists(tutorEmail);
        validateMcqPayload(request.getOptions(), request.getCorrectOptionIndex());

        PracticeQuestion question = buildBaseQuestion(
                request.getTitle(),
                request.getQuestion(),
                request.getDifficultyLevel(),
                request.getTopic(),
                request.getTags(),
                request.getEstimatedSolveTimeMinutes(),
                PracticeQuestionType.PROGRAMMING_MCQ);

        PracticeQuestion savedQuestion = practiceQuestionRepository.save(question);

        ProgrammingMCQ programmingMCQ = new ProgrammingMCQ();
        programmingMCQ.setQuestion(savedQuestion);
        programmingMCQ.setExplanation(request.getExplanation().trim());
        programmingMCQRepository.save(programmingMCQ);

        saveMcqOptions(savedQuestion, request.getOptions(), request.getCorrectOptionIndex());

        PracticeQuestion reloaded = practiceQuestionRepository.findById(savedQuestion.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Practice question not found: " + savedQuestion.getId()));
        return PracticeQuestionResponse.from(reloaded);
    }

    @Transactional
    public PracticeQuestionResponse createAptitudeMcq(TutorCreateAptitudeMcqRequest request, String tutorEmail) {
        ensureTutorExists(tutorEmail);
        validateMcqPayload(request.getOptions(), request.getCorrectOptionIndex());

        PracticeQuestion question = buildBaseQuestion(
                request.getTitle(),
                request.getQuestion(),
                request.getDifficultyLevel(),
                request.getTopic(),
                request.getTags(),
                request.getEstimatedSolveTimeMinutes(),
                PracticeQuestionType.APTITUDE_MCQ);

        PracticeQuestion savedQuestion = practiceQuestionRepository.save(question);

        AptitudeMCQ aptitudeMCQ = new AptitudeMCQ();
        aptitudeMCQ.setQuestion(savedQuestion);
        aptitudeMCQ.setExplanation(request.getExplanation().trim());
        aptitudeMCQRepository.save(aptitudeMCQ);

        saveMcqOptions(savedQuestion, request.getOptions(), request.getCorrectOptionIndex());

        PracticeQuestion reloaded = practiceQuestionRepository.findById(savedQuestion.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Practice question not found: " + savedQuestion.getId()));
        return PracticeQuestionResponse.from(reloaded);
    }

    private PracticeQuestion buildBaseQuestion(
            String title,
            String prompt,
            com.skillforge.common.enums.DifficultyLevel difficultyLevel,
            String topic,
            List<String> tags,
            Integer estimatedSolveTimeMinutes,
            PracticeQuestionType questionType) {

        PracticeQuestion question = new PracticeQuestion();
        question.setQuestionType(questionType);
        question.setTitle(resolveTitle(title, topic, questionType));
        question.setPrompt(prompt.trim());
        question.setDifficultyLevel(difficultyLevel);
        question.setTopic(topic.trim());
        question.setEstimatedSolveTimeMinutes(estimatedSolveTimeMinutes);
        question.setTags(normalizeTags(tags));
        question.setSuccessRate(BigDecimal.ZERO);
        question.setIsActive(true);

        return question;
    }

    private void saveMcqOptions(
            PracticeQuestion question,
            List<TutorMcqOptionCreateRequest> options,
            Integer correctOptionIndex) {
        for (int index = 0; index < options.size(); index++) {
            TutorMcqOptionCreateRequest optionRequest = options.get(index);
            MCQOption option = new MCQOption();
            option.setQuestion(question);
            option.setOptionText(optionRequest.getOptionText().trim());
            option.setDisplayOrder(index + 1);
            option.setIsCorrect((index + 1) == correctOptionIndex);
            mcqOptionRepository.save(option);
        }
    }

    private void validateMcqPayload(List<TutorMcqOptionCreateRequest> options, Integer correctOptionIndex) {
        if (options == null || options.size() != 4) {
            throw new BadRequestException("Exactly 4 options are required");
        }
        if (correctOptionIndex == null || correctOptionIndex < 1 || correctOptionIndex > 4) {
            throw new BadRequestException("correctOptionIndex must be between 1 and 4");
        }
    }

    private Set<String> normalizeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return new LinkedHashSet<>();
        }

        return tags.stream()
                .filter(StringUtils::hasText)
                .map(tag -> tag.trim().toLowerCase())
                .distinct()
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private String resolveTitle(String title, String topic, PracticeQuestionType questionType) {
        if (StringUtils.hasText(title)) {
            return title.trim();
        }
        return switch (questionType) {
            case CODING -> topic.trim() + " - Coding Problem";
            case PROGRAMMING_MCQ -> topic.trim() + " - Programming MCQ";
            case APTITUDE_MCQ -> topic.trim() + " - Aptitude MCQ";
        };
    }

    private void ensureTutorExists(String tutorEmail) {
        userRepository.findByEmail(tutorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + tutorEmail));
    }
}
