package com.skillforge.domain.course.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.skillforge.common.enums.CourseStatus;
import com.skillforge.common.enums.Role;
import com.skillforge.domain.course.dto.CourseChatMessageResponse;
import com.skillforge.domain.course.dto.CourseResponse;
import com.skillforge.domain.course.dto.CreateCourseRequest;
import com.skillforge.domain.course.dto.EnrollmentResponse;
import com.skillforge.domain.course.dto.UpdateCourseRequest;
import com.skillforge.domain.course.entity.Course;
import com.skillforge.domain.course.entity.CourseEnrollment;
import com.skillforge.domain.course.entity.CourseTag;
import com.skillforge.domain.course.repository.CourseEnrollmentRepository;
import com.skillforge.domain.course.repository.CourseRepository;
import com.skillforge.domain.user.entity.User;
import com.skillforge.domain.user.repository.UserRepository;
import com.skillforge.exception.BadRequestException;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.exception.UnauthorizedException;

@Service
public class CourseService {

    private static final Logger log = LoggerFactory.getLogger(CourseService.class);

    private final CourseRepository           courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final UserRepository             userRepository;
    private final Map<Long, List<CourseChatMessageResponse>> chatMessagesByCourse = new ConcurrentHashMap<>();
    private final AtomicLong chatMessageIdSequence = new AtomicLong(1);

    public CourseService(CourseRepository courseRepository,
                         CourseEnrollmentRepository enrollmentRepository,
                         UserRepository userRepository) {
        this.courseRepository  = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository    = userRepository;
    }

    // ── Tutor operations ──────────────────────────────────────────────────

    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request, String tutorEmail) {
        User tutor = findUserByEmail(tutorEmail);

        Course course = new Course();
        course.setTutor(tutor);
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setDifficultyLevel(request.getDifficultyLevel());
        course.setPrice(request.getPrice());
        course.setYoutubeVideoUrl(request.getYoutubeVideoUrl());
        course.setStatus(request.getStatus() != null ? request.getStatus() : CourseStatus.DRAFT);

        Course saved = courseRepository.save(course);

        if (request.getTags() != null) {
            applyTags(saved, request.getTags());
        }

        log.info("Course created: id={}, tutor={}", saved.getId(), tutorEmail);
        return CourseResponse.from(saved);
    }

    @Transactional
    public CourseResponse updateCourse(Long courseId, UpdateCourseRequest request, String tutorEmail) {
        Course course = findCourseById(courseId);
        User tutor = findUserByEmail(tutorEmail);

        if (!course.getTutor().getId().equals(tutor.getId())) {
            throw new UnauthorizedException("You are not the owner of this course");
        }

        if (StringUtils.hasText(request.getTitle())) {
            course.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }
        if (request.getDifficultyLevel() != null) {
            course.setDifficultyLevel(request.getDifficultyLevel());
        }
        if (request.getPrice() != null) {
            course.setPrice(request.getPrice());
        }
        if (request.getYoutubeVideoUrl() != null) {
            course.setYoutubeVideoUrl(request.getYoutubeVideoUrl());
        }
        if (request.getStatus() != null) {
            course.setStatus(request.getStatus());
        }
        if (request.getTags() != null) {
            course.getTags().clear();
            applyTags(course, request.getTags());
        }

        log.info("Course updated: id={}, tutor={}", courseId, tutorEmail);
        return CourseResponse.from(course);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getTutorCourses(String tutorEmail) {
        User tutor = findUserByEmail(tutorEmail);
        return courseRepository.findByTutorId(tutor.getId())
                .stream()
                .map(CourseResponse::from)
                .toList();
    }

    @Transactional
    public void deleteCourse(Long courseId, String tutorEmail) {
        Course course = findCourseById(courseId);
        User tutor = findUserByEmail(tutorEmail);

        if (!course.getTutor().getId().equals(tutor.getId())) {
            throw new UnauthorizedException("You are not the owner of this course");
        }

        courseRepository.delete(course);
        log.info("Course deleted: id={}, tutor={}", courseId, tutorEmail);
    }

    // ── Learner / public operations ───────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CourseResponse> getPublishedCourses() {
        return courseRepository.findByStatus(CourseStatus.PUBLISHED)
                .stream()
                .map(CourseResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long courseId) {
        return CourseResponse.from(findCourseById(courseId));
    }

    @Transactional
    public EnrollmentResponse enroll(Long courseId, String learnerEmail) {
        User learner = findUserByEmail(learnerEmail);
        Course course = findCourseById(courseId);

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new BadRequestException("Cannot enroll in a course that is not published");
        }

        if (enrollmentRepository.existsByCourseIdAndLearnerId(courseId, learner.getId())) {
            throw new BadRequestException("You are already enrolled in this course");
        }

        CourseEnrollment enrollment = new CourseEnrollment(course, learner);
        enrollmentRepository.save(enrollment);

        log.info("Enrollment created: courseId={}, learner={}", courseId, learnerEmail);
        return EnrollmentResponse.from(enrollment);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollments(String learnerEmail) {
        User learner = findUserByEmail(learnerEmail);
        return enrollmentRepository.findByLearnerId(learner.getId())
                .stream()
                .map(EnrollmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseChatMessageResponse> getCourseChatMessages(Long courseId, String requesterEmail) {
        User requester = findUserByEmail(requesterEmail);
        ensureChatAccess(courseId, requester);
        return chatMessagesByCourse.getOrDefault(courseId, List.of());
    }

    @Transactional(readOnly = true)
    public CourseChatMessageResponse postCourseChatMessage(Long courseId, String message, String requesterEmail) {
        User requester = findUserByEmail(requesterEmail);
        Course course = ensureChatAccess(courseId, requester);

        CourseChatMessageResponse chatMessage = new CourseChatMessageResponse(
                chatMessageIdSequence.getAndIncrement(),
                course.getId(),
                requester.getId(),
                (requester.getFirstName() + " " + requester.getLastName()).trim().isEmpty()
                        ? requester.getUsername()
                        : (requester.getFirstName() + " " + requester.getLastName()).trim(),
                requester.getRole().name(),
                message.trim(),
                LocalDateTime.now());

        chatMessagesByCourse.compute(courseId, (id, existing) -> {
            List<CourseChatMessageResponse> messages = existing == null ? new ArrayList<>() : new ArrayList<>(existing);
            messages.add(chatMessage);
            return messages;
        });

        return chatMessage;
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private Course findCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found with id: " + courseId));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + email));
    }

    private Course ensureChatAccess(Long courseId, User requester) {
        Course course = findCourseById(courseId);

        if (requester.getRole() == Role.TUTOR && course.getTutor().getId().equals(requester.getId())) {
            return course;
        }

        if (requester.getRole() == Role.LEARNER
                && enrollmentRepository.existsByCourseIdAndLearnerIdAndCourseTutorId(
                        courseId,
                        requester.getId(),
                        course.getTutor().getId())) {
            return course;
        }

        throw new UnauthorizedException("You must be enrolled in this course to access chat");
    }

    private void applyTags(Course course, List<String> tagNames) {
        tagNames.stream()
                .filter(StringUtils::hasText)
                .map(name -> name.trim().toLowerCase())
                .distinct()
                .map(name -> new CourseTag(course, name))
                .forEach(course.getTags()::add);
    }
}
