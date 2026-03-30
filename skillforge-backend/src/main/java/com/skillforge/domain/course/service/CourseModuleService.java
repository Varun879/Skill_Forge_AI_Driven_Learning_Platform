package com.skillforge.domain.course.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillforge.common.enums.Role;
import com.skillforge.domain.course.dto.CourseModuleProgressResponse;
import com.skillforge.domain.course.dto.CourseModuleRequest;
import com.skillforge.domain.course.dto.CourseModuleResponse;
import com.skillforge.domain.course.entity.Course;
import com.skillforge.domain.course.entity.CourseModule;
import com.skillforge.domain.course.entity.LearnerModuleProgress;
import com.skillforge.domain.course.repository.CourseEnrollmentRepository;
import com.skillforge.domain.course.repository.CourseModuleRepository;
import com.skillforge.domain.course.repository.CourseRepository;
import com.skillforge.domain.course.repository.LearnerModuleProgressRepository;
import com.skillforge.domain.user.entity.User;
import com.skillforge.domain.user.repository.UserRepository;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.exception.UnauthorizedException;

@Service
public class CourseModuleService {

    private final CourseRepository courseRepository;
    private final CourseModuleRepository courseModuleRepository;
    private final LearnerModuleProgressRepository moduleProgressRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    public CourseModuleService(CourseRepository courseRepository,
                               CourseModuleRepository courseModuleRepository,
                               LearnerModuleProgressRepository moduleProgressRepository,
                               CourseEnrollmentRepository enrollmentRepository,
                               UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.courseModuleRepository = courseModuleRepository;
        this.moduleProgressRepository = moduleProgressRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<CourseModuleResponse> getModules(Long courseId, String userEmail) {
        if (userEmail != null && !userEmail.isBlank()) {
            User user = getUserByEmail(userEmail);
            Course course = getCourse(courseId);
            if (user.getRole() == Role.TUTOR && !course.getTutor().getId().equals(user.getId())) {
                boolean enrolled = enrollmentRepository.existsByCourseIdAndLearnerId(courseId, user.getId());
                if (!enrolled) {
                    throw new UnauthorizedException("You are not allowed to view these course modules");
                }
            }
        }

        return courseModuleRepository.findByCourseIdOrderByOrderIndexAsc(courseId)
                .stream()
                .map(CourseModuleResponse::from)
                .toList();
    }

    @Transactional
    public CourseModuleResponse createModule(Long courseId, CourseModuleRequest request, String tutorEmail) {
        User tutor = getUserByEmail(tutorEmail);
        Course course = getCourse(courseId);
        ensureTutorOwnsCourse(tutor, course);

        CourseModule module = new CourseModule();
        module.setCourse(course);
        module.setTitle(request.getTitle().trim());
        module.setContent(request.getContent());
        module.setVideoUrl(request.getVideoUrl());
        module.setOrderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : inferNextOrderIndex(courseId));

        return CourseModuleResponse.from(courseModuleRepository.save(module));
    }

    @Transactional
    public CourseModuleResponse updateModule(Long courseId, Long moduleId, CourseModuleRequest request, String tutorEmail) {
        User tutor = getUserByEmail(tutorEmail);
        CourseModule module = getModule(moduleId);
        if (!module.getCourse().getId().equals(courseId)) {
            throw new ResourceNotFoundException("Module does not belong to course");
        }
        ensureTutorOwnsCourse(tutor, module.getCourse());

        module.setTitle(request.getTitle().trim());
        module.setContent(request.getContent());
        module.setVideoUrl(request.getVideoUrl());
        if (request.getOrderIndex() != null) {
            module.setOrderIndex(request.getOrderIndex());
        }

        return CourseModuleResponse.from(module);
    }

    @Transactional
    public void deleteModule(Long courseId, Long moduleId, String tutorEmail) {
        User tutor = getUserByEmail(tutorEmail);
        CourseModule module = getModule(moduleId);
        if (!module.getCourse().getId().equals(courseId)) {
            throw new ResourceNotFoundException("Module does not belong to course");
        }
        ensureTutorOwnsCourse(tutor, module.getCourse());
        courseModuleRepository.delete(module);
    }

    @Transactional
    public CourseModuleProgressResponse markCompleted(Long courseId, Long moduleId, String learnerEmail) {
        User learner = getUserByEmail(learnerEmail);
        CourseModule module = getModule(moduleId);

        if (!module.getCourse().getId().equals(courseId)) {
            throw new ResourceNotFoundException("Module does not belong to course");
        }

        boolean enrolled = enrollmentRepository.existsByCourseIdAndLearnerId(courseId, learner.getId());
        if (!enrolled) {
            throw new UnauthorizedException("You are not enrolled in this course");
        }

        if (!moduleProgressRepository.existsByModuleIdAndLearnerId(moduleId, learner.getId())) {
            LearnerModuleProgress progress = new LearnerModuleProgress();
            progress.setModule(module);
            progress.setLearner(learner);
            moduleProgressRepository.save(progress);
        }

        return getProgress(courseId, learnerEmail);
    }

    @Transactional(readOnly = true)
    public CourseModuleProgressResponse getProgress(Long courseId, String learnerEmail) {
        User learner = getUserByEmail(learnerEmail);
        long total = courseModuleRepository.countByCourseId(courseId);
        long completed = moduleProgressRepository.countByModuleCourseIdAndLearnerId(courseId, learner.getId());
        return new CourseModuleProgressResponse(courseId, completed, total);
    }

    private int inferNextOrderIndex(Long courseId) {
        List<CourseModule> modules = courseModuleRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        if (modules.isEmpty()) {
            return 1;
        }
        return modules.get(modules.size() - 1).getOrderIndex() + 1;
    }

    private Course getCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    }

    private CourseModule getModule(Long moduleId) {
        return courseModuleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void ensureTutorOwnsCourse(User tutor, Course course) {
        if (tutor.getRole() != Role.TUTOR || !course.getTutor().getId().equals(tutor.getId())) {
            throw new UnauthorizedException("Only the owning tutor may modify this course module");
        }
    }
}
