package com.skillforge.domain.user.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.skillforge.domain.user.dto.UpdateProfileRequest;
import com.skillforge.domain.user.dto.UserProfileResponse;
import com.skillforge.domain.user.entity.OtpEntry;
import com.skillforge.domain.user.entity.User;
import com.skillforge.domain.user.repository.OtpRepository;
import com.skillforge.domain.user.repository.UserRepository;
import com.skillforge.exception.BadRequestException;
import com.skillforge.exception.ResourceNotFoundException;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final long MAX_PROFILE_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Path PROFILE_IMAGE_DIR = Paths.get("data", "profile-images");
    private static final Random OTP_RANDOM = new Random();

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final OtpEmailService otpEmailService;

    @Value("${app.otp.validity-minutes:5}")
    private long otpValidityMinutes;

    public UserService(UserRepository userRepository,
                       OtpRepository otpRepository,
                       OtpEmailService otpEmailService) {
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.otpEmailService = otpEmailService;
    }

    public UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String fullName = request.getFullName().trim();
        int spaceIndex = fullName.indexOf(' ');
        if (spaceIndex > 0) {
            user.setFirstName(fullName.substring(0, spaceIndex));
            user.setLastName(fullName.substring(spaceIndex + 1));
        } else {
            user.setFirstName(fullName);
            user.setLastName("");
        }

        userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public void sendEmailChangeOtp(String currentEmail, String newEmailRaw) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String newEmail = normalizeEmail(newEmailRaw);
        String existingEmail = normalizeEmail(user.getEmail());

        if (newEmail.equals(existingEmail)) {
            throw new BadRequestException("New email must be different from current email");
        }

        if (userRepository.existsByEmail(newEmail)) {
            throw new BadRequestException("This email is already associated with another account");
        }

        boolean recentOtpExists = otpRepository
                .existsByEmailAndUsedFalseAndExpiresAtAfterAndCreatedAtAfter(
                        newEmail, LocalDateTime.now(), LocalDateTime.now().minusSeconds(60));
        if (recentOtpExists) {
            throw new BadRequestException(
                    "An OTP was recently sent. Please wait 60 seconds before requesting another.");
        }

        otpRepository.invalidateAllByEmail(newEmail);

        String otp = generateSecureOtp();
        OtpEntry entry = OtpEntry.builder()
                .email(newEmail)
                .otpCode(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(otpValidityMinutes))
                .used(false)
                .attemptCount(0)
                .build();
        otpRepository.save(entry);

        otpEmailService.sendOtpEmail(newEmail, otp);
        log.info("Email change OTP sent for user={} target={}", currentEmail, newEmail);
    }

    @Transactional
    public UserProfileResponse verifyEmailChangeOtp(String currentEmail, String newEmailRaw, String otp) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String newEmail = normalizeEmail(newEmailRaw);
        String existingEmail = normalizeEmail(user.getEmail());

        if (newEmail.equals(existingEmail)) {
            throw new BadRequestException("New email must be different from current email");
        }

        if (userRepository.existsByEmail(newEmail)) {
            throw new BadRequestException("This email is already associated with another account");
        }

        OtpEntry entry = otpRepository
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(newEmail)
                .orElseThrow(() -> new BadRequestException("No OTP found. Please request a new OTP."));

        if (entry.getExpiresAt().isBefore(LocalDateTime.now())) {
            entry.setUsed(true);
            otpRepository.save(entry);
            throw new BadRequestException("OTP expired. Please request a new OTP.");
        }

        if (entry.getAttemptCount() >= 5) {
            entry.setUsed(true);
            otpRepository.save(entry);
            throw new BadRequestException("Too many failed attempts. Please request a new OTP.");
        }

        if (!entry.getOtpCode().equals(otp)) {
            entry.setAttemptCount(entry.getAttemptCount() + 1);
            otpRepository.save(entry);
            int remaining = 5 - entry.getAttemptCount();
            throw new BadRequestException("Invalid OTP. " + remaining + " attempt(s) remaining.");
        }

        entry.setUsed(true);
        otpRepository.save(entry);

        user.setEmail(newEmail);
        userRepository.save(user);

        log.info("Email updated via OTP verification for user={} newEmail={}", currentEmail, newEmail);
        return toResponse(user);
    }

    @Transactional
    public UserProfileResponse uploadProfileImage(String email, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new BadRequestException("Profile image is required");
        }

        if (image.getSize() > MAX_PROFILE_IMAGE_SIZE_BYTES) {
            throw new BadRequestException("Profile image must be 5MB or smaller");
        }

        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        try {
            Files.createDirectories(PROFILE_IMAGE_DIR);

            String extension = resolveExtension(contentType, Objects.requireNonNullElse(image.getOriginalFilename(), ""));
            String filename = "u" + user.getId() + "-" + UUID.randomUUID() + extension;
            Path destination = PROFILE_IMAGE_DIR.resolve(filename).normalize();

            Files.copy(image.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            String previousAvatar = user.getAvatarUrl();
            String avatarUrl = "/api/user/profile/image/" + filename;
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);

            deletePreviousLocalAvatar(previousAvatar, filename);
            log.info("Profile image updated for user={} file={}", email, filename);
            return toResponse(user);
        } catch (IOException ex) {
            log.error("Failed to store profile image for user={}: {}", email, ex.getMessage());
            throw new BadRequestException("Failed to upload profile image");
        }
    }

    public Resource loadProfileImage(String filename) {
        if (filename == null || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new ResourceNotFoundException("Profile image not found");
        }

        Path filePath = PROFILE_IMAGE_DIR.resolve(filename).normalize();
        if (!Files.exists(filePath)) {
            throw new ResourceNotFoundException("Profile image not found");
        }

        try {
            return new UrlResource(filePath.toUri());
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("Profile image not found");
        }
    }

    private void deletePreviousLocalAvatar(String previousAvatarUrl, String currentFilename) {
        if (previousAvatarUrl == null || previousAvatarUrl.isBlank()) {
            return;
        }

        String prefix = "/api/user/profile/image/";
        if (!previousAvatarUrl.startsWith(prefix)) {
            return;
        }

        String oldFilename = previousAvatarUrl.substring(prefix.length());
        if (oldFilename.equals(currentFilename)) {
            return;
        }

        Path oldFilePath = PROFILE_IMAGE_DIR.resolve(oldFilename).normalize();
        try {
            Files.deleteIfExists(oldFilePath);
        } catch (IOException ex) {
            log.warn("Could not delete previous profile image {}: {}", oldFilename, ex.getMessage());
        }
    }

    private String resolveExtension(String contentType, String originalName) {
        return switch (contentType) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> {
                int dotIndex = originalName.lastIndexOf('.');
                if (dotIndex > -1 && dotIndex < originalName.length() - 1) {
                    yield "." + originalName.substring(dotIndex + 1).toLowerCase();
                }
                yield ".img";
            }
        };
    }

    private String normalizeEmail(String email) {
        return Objects.requireNonNullElse(email, "").toLowerCase().trim();
    }

    private String generateSecureOtp() {
        int number = 100000 + OTP_RANDOM.nextInt(900000);
        return Integer.toString(number);
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getAvatarUrl(),
                user.getRole()
        );
    }
}
