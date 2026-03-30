package com.skillforge.domain.certificate.dto;

import java.time.LocalDateTime;

import com.skillforge.domain.certificate.entity.Certificate;

public class PublicCertificateResponse {

    private Long certificateId;
    private String learnerName;
    private String courseTitle;
    private LocalDateTime issuedAt;
    private String viewUrl;
    private String downloadUrl;

    public static PublicCertificateResponse from(Certificate certificate) {
        PublicCertificateResponse response = new PublicCertificateResponse();
        response.certificateId = certificate.getId();
        response.learnerName = ((certificate.getUser().getFirstName() == null ? "" : certificate.getUser().getFirstName()) + " "
                + (certificate.getUser().getLastName() == null ? "" : certificate.getUser().getLastName())).trim();
        response.courseTitle = certificate.getCourse().getTitle();
        response.issuedAt = certificate.getIssuedAt();
        response.viewUrl = "/api/certificate/public/" + certificate.getPublicToken() + "/file";
        response.downloadUrl = "/api/certificate/public/" + certificate.getPublicToken() + "/download";
        return response;
    }

    public Long getCertificateId() {
        return certificateId;
    }

    public String getLearnerName() {
        return learnerName;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public String getViewUrl() {
        return viewUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }
}
