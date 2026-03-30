package com.skillforge.domain.user.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.skillforge.common.enums.Role;
import com.skillforge.domain.user.dto.AuthResponse;
import com.skillforge.domain.user.dto.GoogleAuthRequest;
import com.skillforge.domain.user.dto.LoginRequest;
import com.skillforge.domain.user.dto.RefreshTokenRequest;
import com.skillforge.domain.user.dto.RegisterRequest;
import com.skillforge.domain.user.dto.SendOtpRequest;
import com.skillforge.domain.user.dto.VerifyOtpRequest;
import com.skillforge.domain.user.entity.OtpEntry;
import com.skillforge.domain.user.entity.RefreshToken;
import com.skillforge.domain.user.entity.User;
import com.skillforge.domain.user.repository.OtpRepository;
import com.skillforge.domain.user.repository.RefreshTokenRepository;
import com.skillforge.domain.user.repository.UserRepository;
import com.skillforge.exception.BadRequestException;
import com.skillforge.exception.DuplicateResourceException;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.exception.UnauthorizedException;
import com.skillforge.security.JwtTokenProvider;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository         userRepository;
    private final OtpRepository          otpRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider       jwtTokenProvider;
    private final PasswordEncoder        passwordEncoder;
    private final AuthenticationManager  authenticationManager;
    private final OtpEmailService        otpEmailService;

    @Value("${app.jwt.refresh-token-expiry}")
    private long refreshTokenExpiryMs;

    @Value("${app.google.client-id}")
    private String googleClientId;
    
    @Value("${app.otp.validity-minutes:5}")
    private long otpValidityMinutes;

    public AuthService(UserRepository userRepository,
                       OtpRepository otpRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       JwtTokenProvider jwtTokenProvider,
                       PasswordEncoder passwordEncoder,
                       @Lazy AuthenticationManager authenticationManager,
                       OtpEmailService otpEmailService) {
        this.userRepository         = userRepository;
        this.otpRepository          = otpRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider       = jwtTokenProvider;
        this.passwordEncoder        = passwordEncoder;
        this.authenticationManager  = authenticationManager;
        this.otpEmailService        = otpEmailService;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // REGISTER
    // ═══════════════════════════════════════════════════════════════════════

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email    = request.getEmail().toLowerCase().trim();
        String username = request.getUsername().trim();

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("An account with this email already exists");
        }
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("This username is already taken");
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .username(username)
                .role(request.getRole())
                .isActive(true)
                .build();

        userRepository.save(user);
        log.info("Registered new {} account for {}", user.getRole(), user.getEmail());
        return buildAuthResponse(user);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // EMAIL + PASSWORD LOGIN
    // ═══════════════════════════════════════════════════════════════════════

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        userRepository.findByEmail(email).ifPresent(user -> {
            boolean googleOnlyAccount = user.getGoogleId() != null
                    && !user.getGoogleId().isBlank()
                    && (user.getPasswordHash() == null || user.getPasswordHash().isBlank());
            if (googleOnlyAccount) {
                throw new UnauthorizedException(
                        "This account uses Google sign-in. Continue with Google or set a password first.");
            }
        });

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        } catch (DisabledException e) {
            throw new UnauthorizedException("Account is disabled. Please contact support.");
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Invalid email or password");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GOOGLE OAUTH
    // ═══════════════════════════════════════════════════════════════════════

    @Transactional
    public AuthResponse googleAuthWithFirebaseToken(String idToken) {
        FirebaseToken decoded = verifyFirebaseToken(idToken);

        String email = decoded.getEmail();
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("Google account email not available from token");
        }

        String normalizedEmail = email.toLowerCase().trim();
        String uid = decoded.getUid();
        String displayName = decoded.getName() != null ? decoded.getName().trim() : "";
        String firstName = extractFirstName(displayName);
        String lastName = extractLastName(displayName);

        User user = userRepository.findByGoogleId(uid)
            .or(() -> userRepository.findByEmail(normalizedEmail))
                .orElseGet(() -> User.builder()
                        .email(normalizedEmail)
                        .googleId(uid)
                        .firstName(firstName)
                        .lastName(lastName)
                        .username(generateUniqueUsername(normalizedEmail))
                        .role(Role.LEARNER)
                        .isActive(true)
                        .build());

        if (user.getGoogleId() != null && !user.getGoogleId().isBlank() && !uid.equals(user.getGoogleId())) {
            throw new UnauthorizedException("This email is already linked to another Google account.");
        }

        if (user.getGoogleId() == null || user.getGoogleId().isBlank()) {
            user.setGoogleId(uid);
        }

        if (user.getFirstName() == null || user.getFirstName().isBlank()) {
            user.setFirstName(firstName);
        }

        if (user.getLastName() == null || user.getLastName().isBlank()) {
            user.setLastName(lastName);
        }

        userRepository.save(user);
        log.info("Firebase Google auth for {}", user.getEmail());
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse googleAuthWithOAuthProfile(String email, String googleId, String fullName, String avatarUrl) {
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("Google account email not available from OAuth profile");
        }

        String normalizedEmail = email.toLowerCase().trim();
        String firstName = extractFirstName(fullName);
        String lastName = extractLastName(fullName);

        User user = userRepository.findByGoogleId(googleId)
            .or(() -> userRepository.findByEmail(normalizedEmail))
                .orElseGet(() -> User.builder()
                        .email(normalizedEmail)
                        .googleId(googleId)
                        .firstName(firstName)
                        .lastName(lastName)
                        .username(generateUniqueUsername(normalizedEmail))
                        .avatarUrl(avatarUrl)
                        .role(Role.LEARNER)
                        .isActive(true)
                        .build());

        if (googleId != null && !googleId.isBlank()) {
            if (user.getGoogleId() != null && !user.getGoogleId().isBlank() && !googleId.equals(user.getGoogleId())) {
                throw new UnauthorizedException("This email is already linked to another Google account.");
            }
            if (user.getGoogleId() == null || user.getGoogleId().isBlank()) {
                user.setGoogleId(googleId);
            }
        }
        if (user.getFirstName() == null || user.getFirstName().isBlank()) {
            user.setFirstName(firstName);
        }
        if (user.getLastName() == null || user.getLastName().isBlank()) {
            user.setLastName(lastName);
        }
        if ((user.getAvatarUrl() == null || user.getAvatarUrl().isBlank())
                && avatarUrl != null && !avatarUrl.isBlank()) {
            user.setAvatarUrl(avatarUrl);
        }

        userRepository.save(user);
        log.info("OAuth Google auth success for {}", user.getEmail());
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse googleAuth(GoogleAuthRequest request) {
        GoogleIdToken.Payload payload = verifyGoogleIdToken(request.getIdToken());

        String email    = payload.getEmail().toLowerCase().trim();
        String googleId = payload.getSubject();

        Optional<User> existing = userRepository.findByGoogleId(googleId);
        if (existing.isEmpty()) {
            existing = userRepository.findByEmail(email);
        }

        User user = existing.orElseGet(() -> {
            if (userRepository.existsByEmail(email)) {
                throw new DuplicateResourceException(
                        "An account with this email already exists. Please log in with password.");
            }
            String firstName = castOrDefault(payload.get("given_name"), "User");
            String lastName  = castOrDefault(payload.get("family_name"), "");
            String avatarUrl = castOrDefault(payload.get("picture"), null);
            Role   role      = request.getRole() != null ? request.getRole() : Role.LEARNER;

            log.info("First-time Google sign-up for {} as {}", email, role);
            return User.builder()
                    .email(email)
                    .googleId(googleId)
                    .firstName(firstName)
                    .lastName(lastName)
                    .username(generateUniqueUsername(email))
                    .avatarUrl(avatarUrl)
                    .role(role)
                    .isActive(true)
                    .build();
        });

        if (user.getGoogleId() == null) {
            user.setGoogleId(googleId);
        }

        userRepository.save(user);
        log.info("Google auth for {}", user.getEmail());
        return buildAuthResponse(user);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SEND OTP
    // ═══════════════════════════════════════════════════════════════════════

    @Transactional
    public void sendOtp(SendOtpRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        log.info("OTP request received for {}", email);

        boolean recentOtpExists = otpRepository
                .existsByEmailAndUsedFalseAndExpiresAtAfterAndCreatedAtAfter(
                        email, LocalDateTime.now(), LocalDateTime.now().minusSeconds(60));
        if (recentOtpExists) {
            throw new BadRequestException(
                    "An OTP was recently sent. Please wait 60 seconds before requesting another.");
        }

        otpRepository.invalidateAllByEmail(email);

        String   otp   = generateSecureOtp();
        OtpEntry entry = OtpEntry.builder()
                .email(email)
                .otpCode(otp)
            .expiresAt(LocalDateTime.now().plusMinutes(otpValidityMinutes))
                .used(false)
                .attemptCount(0)
                .build();
        otpRepository.save(entry);

        otpEmailService.sendOtpEmail(email, otp);
        log.info("OTP issued for {} with {} minute expiry", email, otpValidityMinutes);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // VERIFY OTP
    // ═══════════════════════════════════════════════════════════════════════

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        log.info("OTP verification attempt for {}", email);

        OtpEntry entry = otpRepository
            .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new BadRequestException(
                "No OTP found. Please request a new OTP."));

        if (entry.getExpiresAt().isBefore(LocalDateTime.now())) {
            entry.setUsed(true);
            otpRepository.save(entry);
            log.warn("OTP verification failed: expired OTP for {}", email);
            throw new BadRequestException("OTP expired. Please request a new OTP.");
        }

        if (entry.getAttemptCount() >= 5) {
            entry.setUsed(true);
            otpRepository.save(entry);
            log.warn("OTP verification failed: attempts exceeded for {}", email);
            throw new BadRequestException("Too many failed attempts. Please request a new OTP.");
        }

        if (!entry.getOtpCode().equals(request.getOtp())) {
            entry.setAttemptCount(entry.getAttemptCount() + 1);
            otpRepository.save(entry);
            int remaining = 5 - entry.getAttemptCount();
            log.warn("OTP verification failed: invalid OTP for {} (remaining={})", email, remaining);
            throw new BadRequestException("Invalid OTP. " + remaining + " attempt(s) remaining.");
        }

        entry.setUsed(true);
        otpRepository.save(entry);

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            log.info("Auto-creating account for OTP-only sign-up: {}", email);
            return userRepository.save(User.builder()
                    .email(email)
                    .firstName("User")
                    .lastName("")
                    .username(generateUniqueUsername(email))
                    .role(Role.LEARNER)
                    .isActive(true)
                    .build());
        });

        log.info("OTP verified successfully for {}", email);
        return buildAuthResponse(user);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // REFRESH TOKEN  (token rotation — old token revoked on use)
    // ═══════════════════════════════════════════════════════════════════════

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String hash = sha256Hex(request.getRefreshToken());

        RefreshToken stored = refreshTokenRepository
                .findByTokenHashAndRevokedFalseAndExpiresAtAfter(hash, LocalDateTime.now())
                .orElseThrow(() -> new UnauthorizedException(
                        "Refresh token is invalid or has expired. Please log in again."));

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        log.info("Refresh token rotated for user id={}", stored.getUser().getId());
        return buildAuthResponse(stored.getUser());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════════════

    private AuthResponse buildAuthResponse(User user) {
        String accessToken     = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole());
        String rawRefreshToken = jwtTokenProvider.generateOpaqueRefreshToken();

        RefreshToken stored = RefreshToken.builder()
                .user(user)
                .tokenHash(sha256Hex(rawRefreshToken))
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiryMs / 1000))
                .revoked(false)
                .build();
        refreshTokenRepository.save(stored);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiryMs() / 1000)
            .role(user.getRole().name())
            .userId(user.getId())
            .name((user.getFirstName() + " " + user.getLastName()).trim())
                .user(AuthResponse.UserSummary.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .avatarUrl(user.getAvatarUrl())
                        .role(user.getRole())
                        .build())
                .build();
    }

    private GoogleIdToken.Payload verifyGoogleIdToken(String rawIdToken) {
        try {
            GoogleIdTokenVerifier.Builder verifierBuilder = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance());

            if (googleClientId != null && !googleClientId.isBlank()) {
                List<String> audiences = Arrays.stream(googleClientId.split(","))
                        .map(String::trim)
                        .filter(value -> !value.isBlank())
                        .toList();
                if (!audiences.isEmpty()) {
                    verifierBuilder.setAudience(audiences);
                }
            } else {
                log.warn("GOOGLE_CLIENT_ID is not configured; verifying Google ID token without audience restriction");
            }

            GoogleIdTokenVerifier verifier = verifierBuilder.build();

            GoogleIdToken idToken = verifier.verify(rawIdToken);
            if (idToken == null) {
                throw new UnauthorizedException("Invalid Google ID token");
            }
            return idToken.getPayload();
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google token verification error: {}", e.getMessage());
            throw new UnauthorizedException("Google authentication failed");
        }
    }

    private String generateSecureOtp() {
        return String.valueOf(100_000 + new SecureRandom().nextInt(900_000));
    }

    private String generateUniqueUsername(String email) {
        String base      = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "_");
        String candidate = base;
        int    suffix    = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix++;
        }
        return candidate;
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes     = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available on this JVM", e);
        }
    }

    private String castOrDefault(Object value, String defaultValue) {
        return value instanceof String s ? s : defaultValue;
    }

    private FirebaseToken verifyFirebaseToken(String idToken) {
        try {
            return FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            throw new UnauthorizedException("Invalid Firebase ID token");
        }
    }

    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "User";
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts[0];
    }

    private String extractLastName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "Google";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length <= 1) {
            return "Google";
        }
        return String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
    }
}
