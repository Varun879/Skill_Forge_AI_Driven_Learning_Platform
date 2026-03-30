package com.skillforge.domain.courseexam.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillforge.common.enums.CourseStatus;
import com.skillforge.common.enums.PracticeQuestionType;
import com.skillforge.common.enums.Role;
import com.skillforge.domain.course.entity.Course;
import com.skillforge.domain.course.repository.CourseEnrollmentRepository;
import com.skillforge.domain.course.repository.CourseRepository;
import com.skillforge.domain.courseexam.dto.CourseExamResponse;
import com.skillforge.domain.courseexam.dto.CreateCourseExamRequest;
import com.skillforge.domain.courseexam.entity.CourseExam;
import com.skillforge.domain.courseexam.entity.CourseExamQuestion;
import com.skillforge.domain.courseexam.repository.CourseExamRepository;
import com.skillforge.domain.practice.entity.PracticeQuestion;
import com.skillforge.domain.practice.repository.PracticeQuestionRepository;
import com.skillforge.domain.user.entity.User;
import com.skillforge.domain.user.repository.UserRepository;
import com.skillforge.exception.BadRequestException;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.exception.UnauthorizedException;

@Service
public class CourseExamService {

    private final CourseExamRepository courseExamRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final PracticeQuestionRepository practiceQuestionRepository;

    public CourseExamService(CourseExamRepository courseExamRepository,
                             CourseRepository courseRepository,
                             UserRepository userRepository,
                             CourseEnrollmentRepository courseEnrollmentRepository,
                             PracticeQuestionRepository practiceQuestionRepository) {
        this.courseExamRepository = courseExamRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.courseEnrollmentRepository = courseEnrollmentRepository;
        this.practiceQuestionRepository = practiceQuestionRepository;
    }

    @Transactional
    public CourseExamResponse createExam(Long courseId, CreateCourseExamRequest request, String tutorEmail) {
        User tutor = findUserByEmail(tutorEmail);
        Course course = findCourseById(courseId);
        ensureTutorOwnsCourse(course, tutor);

        List<Long> rawQuestionIds = request.getQuestionIds() == null ? List.of() : request.getQuestionIds();
        List<Long> questionIds = rawQuestionIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (questionIds.size() < 3) {
            throw new BadRequestException("Exam must contain at least 3 unique questions");
        }

        Collection<PracticeQuestionType> allowedTypes = List.of(
                PracticeQuestionType.PROGRAMMING_MCQ,
                PracticeQuestionType.APTITUDE_MCQ);

        List<PracticeQuestion> questions = practiceQuestionRepository.findByIdInAndQuestionTypeInAndIsActiveTrue(questionIds, allowedTypes);
        if (questions.size() != questionIds.size()) {
            throw new BadRequestException("One or more selected questions are invalid or inactive");
        }

        Map<Long, PracticeQuestion> byId = new HashMap<>();
        for (PracticeQuestion question : questions) {
            byId.put(question.getId(), question);
        }

        CourseExam exam = new CourseExam();
        exam.setCourse(course);
        exam.setTutor(tutor);
        exam.setTitle(request.getTitle().trim());
        exam.setDescription(request.getDescription());
        exam.setDurationMinutes(request.getDurationMinutes());
        exam.setIsPublished(Boolean.TRUE.equals(request.getPublished()));

        for (int i = 0; i < questionIds.size(); i++) {
            PracticeQuestion question = byId.get(questionIds.get(i));
            CourseExamQuestion examQuestion = new CourseExamQuestion();
            examQuestion.setCourseExam(exam);
            examQuestion.setQuestion(question);
            examQuestion.setQuestionOrder(i + 1);
            exam.getQuestions().add(examQuestion);
        }

        CourseExam saved = courseExamRepository.save(exam);
        return CourseExamResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<CourseExamResponse> listExams(Long courseId, String requesterEmail) {
        Course course = findCourseById(courseId);

        List<CourseExam> exams;
        if (requesterEmail == null || requesterEmail.isBlank()) {
            if (course.getStatus() != CourseStatus.PUBLISHED) {
                throw new UnauthorizedException("Only published course exams are visible");
            }
            exams = courseExamRepository.findByCourseIdAndIsPublishedTrueOrderByCreatedAtDesc(courseId);
            return exams.stream().map(CourseExamResponse::from).toList();
        }

        User requester = findUserByEmail(requesterEmail);
        if (requester.getRole() == Role.TUTOR && course.getTutor().getId().equals(requester.getId())) {
            exams = courseExamRepository.findByCourseIdOrderByCreatedAtDesc(courseId);
            return exams.stream().map(CourseExamResponse::from).toList();
        }

        boolean isEnrolledLearner = requester.getRole() == Role.LEARNER
                && courseEnrollmentRepository.existsByCourseIdAndLearnerId(courseId, requester.getId());

        if (!isEnrolledLearner) {
            throw new UnauthorizedException("You are not allowed to view these course exams");
        }

        exams = courseExamRepository.findByCourseIdAndIsPublishedTrueOrderByCreatedAtDesc(courseId);
        return exams.stream().map(CourseExamResponse::from).toList();
    }

    private Course findCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private void ensureTutorOwnsCourse(Course course, User tutor) {
        if (tutor.getRole() != Role.TUTOR || !course.getTutor().getId().equals(tutor.getId())) {
            throw new UnauthorizedException("Only the course tutor can create exams for this course");
        }
    }
}
