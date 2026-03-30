package com.skillforge.domain.course.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillforge.common.response.ApiResponse;
import com.skillforge.domain.course.dto.CourseResponse;
import com.skillforge.domain.course.dto.CourseChatMessageResponse;
import com.skillforge.domain.course.dto.CreateCourseChatMessageRequest;
import com.skillforge.domain.course.dto.CreateCourseRequest;
import com.skillforge.domain.course.dto.EnrollRequest;
import com.skillforge.domain.course.dto.EnrollmentResponse;
import com.skillforge.domain.course.dto.UpdateCourseRequest;
import com.skillforge.domain.course.service.CourseService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * POST /api/courses
     * Create a new course (draft or published). Tutor only.
     */
    @PostMapping
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @Valid @RequestBody CreateCourseRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        CourseResponse course = courseService.createCourse(request, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Course created successfully", course));
    }

    /**
     * PUT /api/courses/{id}
     * Update an existing course. Only the owning tutor may edit.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        CourseResponse course = courseService.updateCourse(id, request, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Course updated successfully", course));
    }

    /**
     * GET /api/courses
     * List all published courses. Public.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getCourses() {
        List<CourseResponse> courses = courseService.getPublishedCourses();
        return ResponseEntity.ok(ApiResponse.ok(courses));
    }

    /**
     * GET /api/courses/mine
     * List all courses owned by the authenticated tutor.
     */
    @GetMapping("/mine")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getTutorCourses(
            @AuthenticationPrincipal UserDetails principal) {
        List<CourseResponse> courses = courseService.getTutorCourses(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(courses));
    }

    /**
     * GET /api/courses/{id}
     * Get a single course by ID. Public.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourse(@PathVariable Long id) {
        CourseResponse course = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResponse.ok(course));
    }

    /**
     * DELETE /api/courses/{id}
     * Delete an existing course. Only the owning tutor may delete.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        courseService.deleteCourse(id, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Course deleted successfully"));
    }

    /**
     * POST /api/courses/enroll
     * Enroll the authenticated learner in a published course.
     */
    @PostMapping("/enroll")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enroll(
            @Valid @RequestBody EnrollRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        EnrollmentResponse enrollment = courseService.enroll(request.getCourseId(), principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Enrolled successfully", enrollment));
    }

    /**
     * POST /api/courses/{id}/enroll
     * Enrollment alias for clients posting with path variable.
     */
    @PostMapping("/{id}/enroll")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enrollByPath(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        EnrollmentResponse enrollment = courseService.enroll(id, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Enrolled successfully", enrollment));
    }

    /**
     * GET /api/courses/{id}/chat
     * Returns course chat messages for enrolled learners or owning tutor.
     */
    @GetMapping("/{id}/chat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CourseChatMessageResponse>>> getCourseChat(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        List<CourseChatMessageResponse> messages = courseService.getCourseChatMessages(id, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(messages));
    }

    /**
     * POST /api/courses/{id}/chat
     * Adds a new chat message for enrolled learners or owning tutor.
     */
    @PostMapping("/{id}/chat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CourseChatMessageResponse>> postCourseChat(
            @PathVariable Long id,
            @Valid @RequestBody CreateCourseChatMessageRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        CourseChatMessageResponse message = courseService.postCourseChatMessage(id, request.getMessage(), principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Message sent", message));
    }
}
