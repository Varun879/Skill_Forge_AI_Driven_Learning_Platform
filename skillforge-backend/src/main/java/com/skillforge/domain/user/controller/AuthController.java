package com.skillforge.domain.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

import com.skillforge.common.response.ApiResponse;
import com.skillforge.domain.user.dto.AuthResponse;
import com.skillforge.domain.user.dto.LoginRequest;
import com.skillforge.domain.user.dto.RefreshTokenRequest;
import com.skillforge.domain.user.dto.RegisterRequest;
import com.skillforge.domain.user.dto.SendOtpRequest;
import com.skillforge.domain.user.dto.VerifyOtpRequest;
import com.skillforge.domain.user.service.AuthService;
import com.skillforge.exception.UnauthorizedException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${app.otp.validity-minutes:5}")
    private long otpValidityMinutes;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** POST /api/auth/register — email + password registration, returns 201 + JWT tokens. */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse auth = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Registration successful", auth));
    }

    /** POST /api/auth/login — email + password login. */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse auth = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", auth));
    }

    /** POST /api/auth/google — verify Firebase Google ID token from Bearer auth header. */
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> googleAuth(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }
        String idToken = authorizationHeader.substring(7).trim();
        AuthResponse auth = authService.googleAuthWithFirebaseToken(idToken);
        return ResponseEntity.ok(ApiResponse.ok("Google authentication successful", auth));
    }

    /** POST /api/auth/send-otp — email a 6-digit OTP (rate-limited: 1 per 60 s). */
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(
            @Valid @RequestBody SendOtpRequest request) {
        authService.sendOtp(request);
        return ResponseEntity.ok(
            ApiResponse.ok("OTP sent to " + request.getEmail() + ". Valid for " + otpValidityMinutes + " minutes."));
    }

    /** POST /api/auth/verify-otp — verify OTP, returns JWT tokens. */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse auth = authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("OTP verified successfully", auth));
    }

    /**
     * POST /api/auth/refresh-token — rotate refresh token and issue new access + refresh pair.
     * The consumed refresh token is revoked immediately.
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse auth = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed successfully", auth));
    }
}
