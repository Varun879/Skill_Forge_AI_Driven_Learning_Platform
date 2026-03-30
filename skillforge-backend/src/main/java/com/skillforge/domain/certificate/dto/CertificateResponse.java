package com.skillforge.domain.certificate.dto;

import java.time.LocalDateTime;

import com.skillforge.domain.certificate.entity.Certificate;

public class CertificateResponse {

    private Long id;
    private Long userId;
    private Long courseId;
    private String courseTitle;
    private LocalDateTime issuedAt;
    private String certificateUrl;
    private String certificateDownloadUrl;
    private String publicVerifyUrl;

    public static CertificateResponse from(Certificate certificate) {
        CertificateResponse out = new CertificateResponse();
        out.id = certificate.getId();
        out.userId = certificate.getUser().getId();
        out.courseId = certificate.getCourse().getId();
        out.courseTitle = certificate.getCourse().getTitle();
        out.issuedAt = certificate.getIssuedAt();
        out.certificateUrl = certificate.getCertificateUrl();
        out.certificateDownloadUrl = certificate.getCertificateUrl() + "?download=true";
        if (certificate.getPublicToken() != null && !certificate.getPublicToken().isBlank()) {
            out.publicVerifyUrl = "/api/certificate/public/" + certificate.getPublicToken() + "/file";
        }
        return out;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public String getCertificateUrl() {
        return certificateUrl;
    }

    public String getCertificateDownloadUrl() {
        return certificateDownloadUrl;
    }

    public String getPublicVerifyUrl() {
        return publicVerifyUrl;
    }
}
