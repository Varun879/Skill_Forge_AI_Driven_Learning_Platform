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
import com.skillforge.domain.course.dto.CourseModuleProgressResponse;
import com.skillforge.domain.course.dto.CourseModuleRequest;
import com.skillforge.domain.course.dto.CourseModuleResponse;
import com.skillforge.domain.course.service.CourseModuleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/courses/{courseId}/modules")
public class CourseModuleController {

    private final CourseModuleService courseModuleService;

    public CourseModuleController(CourseModuleService courseModuleService) {
        this.courseModuleService = courseModuleService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseModuleResponse>>> getModules(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails principal) {
        String email = principal != null ? principal.getUsername() : null;
        List<CourseModuleResponse> modules = courseModuleService.getModules(courseId, email);
        return ResponseEntity.ok(ApiResponse.ok(modules));
    }

    @PostMapping
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<ApiResponse<CourseModuleResponse>> createModule(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseModuleRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        CourseModuleResponse created = courseModuleService.createModule(courseId, request, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Module created", created));
    }

    @PutMapping("/{moduleId}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<ApiResponse<CourseModuleResponse>> updateModule(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @Valid @RequestBody CourseModuleRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        CourseModuleResponse updated = courseModuleService.updateModule(courseId, moduleId, request, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Module updated", updated));
    }

    @DeleteMapping("/{moduleId}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<ApiResponse<Void>> deleteModule(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @AuthenticationPrincipal UserDetails principal) {
        courseModuleService.deleteModule(courseId, moduleId, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Module deleted"));
    }

    @PostMapping("/{moduleId}/complete")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<CourseModuleProgressResponse>> markComplete(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @AuthenticationPrincipal UserDetails principal) {
        CourseModuleProgressResponse progress = courseModuleService.markCompleted(courseId, moduleId, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Module marked complete", progress));
    }

    @GetMapping("/progress")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<CourseModuleProgressResponse>> getProgress(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails principal) {
        CourseModuleProgressResponse progress = courseModuleService.getProgress(courseId, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(progress));
    }
}
