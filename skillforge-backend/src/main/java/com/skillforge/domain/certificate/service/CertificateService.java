package com.skillforge.domain.certificate.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.skillforge.domain.certificate.dto.CertificateResponse;
import com.skillforge.domain.certificate.entity.Certificate;
import com.skillforge.domain.certificate.repository.CertificateRepository;
import com.skillforge.domain.course.entity.Course;
import com.skillforge.domain.course.repository.CourseEnrollmentRepository;
import com.skillforge.domain.user.entity.User;
import com.skillforge.domain.user.repository.UserRepository;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.exception.UnauthorizedException;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    @Value("${certificate.storage-path:data/certificates}")
    private String certificateStoragePath;

    @Value("${app.backend.public-base-url:http://localhost:8080}")
    private String backendPublicBaseUrl;

    public CertificateService(CertificateRepository certificateRepository,
                              CourseEnrollmentRepository enrollmentRepository,
                              UserRepository userRepository) {
        this.certificateRepository = certificateRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<CertificateResponse> getMyCertificates(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return certificateRepository.findByUserIdOrderByIssuedAtDesc(user.getId())
            .stream()
            .map(CertificateResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public byte[] readOwnedCertificateFile(String email, String fileName) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String certificateUrl = "/api/certificate/files/" + fileName;
        certificateRepository.findByUserIdAndCertificateUrl(user.getId(), certificateUrl)
            .orElseThrow(() -> new UnauthorizedException("You do not have access to this certificate file"));

        return readCertificateFile(fileName);
    }

    @Transactional
    public Certificate issueCourseCertificate(User learner, Course course, BigDecimal scorePercent) {
        if (!enrollmentRepository.existsByCourseIdAndLearnerId(course.getId(), learner.getId())) {
            throw new UnauthorizedException("Learner is not enrolled in this course");
        }

        return certificateRepository.findByUserIdAndCourseId(learner.getId(), course.getId())
                .orElseGet(() -> createCertificate(learner, course, scorePercent));
    }

    @Transactional(readOnly = true)
    public Certificate getCertificateByToken(String publicToken) {
        return certificateRepository.findByPublicToken(publicToken)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found"));
    }

    @Transactional(readOnly = true)
    public byte[] readPublicCertificateFile(String publicToken) {
        Certificate certificate = getCertificateByToken(publicToken);
        String fileName = extractFileName(certificate.getCertificateUrl());
        return readCertificateFile(fileName);
    }

    private Certificate createCertificate(User learner, Course course, BigDecimal scorePercent) {
        String fileName = buildFileName(learner.getId(), course.getId());
        String publicToken = UUID.randomUUID().toString().replace("-", "");
        String verifyUrl = normalizeBaseUrl(backendPublicBaseUrl) + "/api/certificate/public/" + publicToken + "/file";

        writeCertificateSvg(fileName, learner, course.getTitle(), scorePercent, verifyUrl);

        Certificate certificate = new Certificate();
        certificate.setUser(learner);
        certificate.setCourse(course);
        certificate.setCertificateUrl("/api/certificate/files/" + fileName);
        certificate.setPublicToken(publicToken);
        return certificateRepository.save(certificate);
    }

    private String buildFileName(Long userId, Long courseId) {
        return "certificate_u" + userId + "_c" + courseId + "_" + System.currentTimeMillis() + ".svg";
    }

    private void writeCertificateSvg(String fileName,
                                     User user,
                                     String courseTitle,
                                     BigDecimal scorePercent,
                                     String verifyUrl) {
        try {
            Path baseDir = Paths.get(certificateStoragePath);
            Files.createDirectories(baseDir);
            Path output = baseDir.resolve(fileName);

                String fullName = ((user.getFirstName() == null ? "" : user.getFirstName()) + " "
                    + (user.getLastName() == null ? "" : user.getLastName())).trim();
            String date = LocalDate.now().toString();
            String qrSvg = renderQrAsSvg(verifyUrl, 220, 965, 665);
            int score = scorePercent != null ? scorePercent.intValue() : 0;
            String safeVerifyUrl = escape(verifyUrl);

                List<String> lines = List.of(
                    "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"1400\" height=\"990\">",
                    "<rect width=\"100%\" height=\"100%\" fill=\"#f8fafc\"/>",
                    "<rect x=\"40\" y=\"40\" width=\"1320\" height=\"910\" fill=\"white\" stroke=\"#0f172a\" stroke-width=\"4\" rx=\"16\"/>",
                    "<text x=\"700\" y=\"180\" text-anchor=\"middle\" font-size=\"54\" font-family=\"Georgia\" fill=\"#0f172a\">SkillForge Certificate</text>",
                    "<text x=\"700\" y=\"270\" text-anchor=\"middle\" font-size=\"30\" font-family=\"Arial\" fill=\"#334155\">This certifies that</text>",
                    "<text x=\"700\" y=\"360\" text-anchor=\"middle\" font-size=\"52\" font-family=\"Georgia\" fill=\"#0f172a\">" + escape(fullName) + "</text>",
                    "<text x=\"700\" y=\"435\" text-anchor=\"middle\" font-size=\"30\" font-family=\"Arial\" fill=\"#334155\">has successfully completed</text>",
                    "<text x=\"700\" y=\"520\" text-anchor=\"middle\" font-size=\"42\" font-family=\"Georgia\" fill=\"#0f172a\">" + escape(courseTitle) + "</text>",
                    "<text x=\"700\" y=\"595\" text-anchor=\"middle\" font-size=\"26\" font-family=\"Arial\" fill=\"#334155\">Final exam score: " + score + "%</text>",
                    "<text x=\"700\" y=\"690\" text-anchor=\"middle\" font-size=\"24\" font-family=\"Arial\" fill=\"#334155\">Issued on " + date + "</text>",
                    "<text x=\"1075\" y=\"650\" text-anchor=\"middle\" font-size=\"20\" font-family=\"Arial\" fill=\"#0f172a\">Scan to verify</text>",
                    qrSvg,
                    "<text x=\"1075\" y=\"915\" text-anchor=\"middle\" font-size=\"12\" font-family=\"Arial\" fill=\"#64748b\">" + safeVerifyUrl + "</text>",
                    "<text x=\"700\" y=\"820\" text-anchor=\"middle\" font-size=\"20\" font-family=\"Arial\" fill=\"#64748b\">Generated by SkillForge</text>",
                    "</svg>"
                );

            Files.writeString(output, String.join("\n", lines), StandardCharsets.UTF_8);
        } catch (Exception ignored) {
        }
    }

    private byte[] readCertificateFile(String fileName) {
        try {
            Path path = Paths.get(certificateStoragePath).resolve(fileName).normalize();
            return Files.readAllBytes(path);
        } catch (Exception ignored) {
            throw new ResourceNotFoundException("Certificate file not found");
        }
    }

    private String extractFileName(String certificateUrl) {
        int slashIndex = certificateUrl.lastIndexOf('/');
        if (slashIndex < 0 || slashIndex + 1 >= certificateUrl.length()) {
            throw new ResourceNotFoundException("Certificate file not found");
        }
        return certificateUrl.substring(slashIndex + 1);
    }

    private String normalizeBaseUrl(String value) {
        if (value == null || value.isBlank()) {
            return "http://localhost:8080";
        }
        String trimmed = value.trim();
        if (trimmed.endsWith("/")) {
            return trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private String renderQrAsSvg(String value, int size, int x, int y) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(value, BarcodeFormat.QR_CODE, size, size);

        int width = matrix.getWidth();
        int height = matrix.getHeight();
        double moduleW = (double) size / (double) width;
        double moduleH = (double) size / (double) height;

        StringBuilder path = new StringBuilder();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (matrix.get(col, row)) {
                    double px = x + (col * moduleW);
                    double py = y + (row * moduleH);
                    path.append("M").append(round2(px)).append(" ").append(round2(py))
                            .append("h").append(round2(moduleW)).append("v").append(round2(moduleH))
                            .append("h-").append(round2(moduleW)).append("z");
                }
            }
        }

        return "<a href=\"" + escape(value) + "\" target=\"_blank\" rel=\"noopener noreferrer\">"
                + "<rect x=\"" + x + "\" y=\"" + y + "\" width=\"" + size + "\" height=\"" + size + "\" fill=\"white\" stroke=\"#0f172a\" stroke-width=\"2\"/>"
                + "<path d=\"" + path + "\" fill=\"#0f172a\"/>"
                + "</a>";
    }

    private String round2(double value) {
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    private String escape(String value) {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }
}
