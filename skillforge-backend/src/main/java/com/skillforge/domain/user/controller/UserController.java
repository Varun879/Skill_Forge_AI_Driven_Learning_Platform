package com.skillforge.domain.user.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.skillforge.common.response.ApiResponse;
import com.skillforge.domain.course.dto.EnrollmentResponse;
import com.skillforge.domain.course.service.CourseService;
import com.skillforge.domain.user.dto.SendEmailChangeOtpRequest;
import com.skillforge.domain.user.dto.UpdateProfileRequest;
import com.skillforge.domain.user.dto.UserProfileResponse;
import com.skillforge.domain.user.dto.VerifyEmailChangeOtpRequest;
import com.skillforge.domain.user.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final CourseService courseService;

    @Value("${app.otp.validity-minutes:5}")
    private long otpValidityMinutes;

    public UserController(UserService userService, CourseService courseService) {
        this.userService = userService;
        this.courseService = courseService;
    }

    /** GET /api/user/me — return the authenticated user's profile. */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserProfileResponse profile = userService.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Profile retrieved", profile));
    }

    /** GET /api/user/enrollments — return learner enrollments. */
    @GetMapping("/enrollments")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getEnrollments(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<EnrollmentResponse> enrollments = courseService.getEnrollments(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(enrollments));
    }

    /** PUT /api/user/me — update the authenticated user's display name. */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse profile = userService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", profile));
    }

    @PostMapping({"/profile/upload", "/upload-profile-image"})
    public ResponseEntity<ApiResponse<UserProfileResponse>> uploadProfileImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        MultipartFile upload = image != null ? image : file;
        UserProfileResponse profile = userService.uploadProfileImage(userDetails.getUsername(), upload);
        return ResponseEntity.ok(ApiResponse.ok("Profile image uploaded successfully", profile));
    }

    @GetMapping("/profile/image/{filename:.+}")
    public ResponseEntity<Resource> getProfileImage(@PathVariable String filename) {
        Resource resource = userService.loadProfileImage(filename);
        MediaType mediaType = MediaTypeFactory.getMediaType(filename).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.noCache())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }

    @PostMapping("/email/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendEmailChangeOtp(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SendEmailChangeOtpRequest request) {
        userService.sendEmailChangeOtp(userDetails.getUsername(), request.getNewEmail());
        return ResponseEntity.ok(ApiResponse.ok(
                "OTP sent to " + request.getNewEmail() + ". Valid for " + otpValidityMinutes + " minutes."));
    }

    @PostMapping("/email/verify-otp")
    public ResponseEntity<ApiResponse<UserProfileResponse>> verifyEmailChangeOtp(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody VerifyEmailChangeOtpRequest request) {
        UserProfileResponse profile = userService.verifyEmailChangeOtp(
                userDetails.getUsername(), request.getNewEmail(), request.getOtp());
        return ResponseEntity.ok(ApiResponse.ok("Email updated successfully", profile));
    }
}
