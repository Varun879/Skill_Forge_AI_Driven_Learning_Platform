package com.skillforge.domain.certificate.controller;

import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillforge.common.response.ApiResponse;
import com.skillforge.domain.certificate.dto.CertificateResponse;
import com.skillforge.domain.certificate.dto.PublicCertificateResponse;
import com.skillforge.domain.certificate.entity.Certificate;
import com.skillforge.domain.certificate.service.CertificateService;

@RestController
@RequestMapping("/api/certificate")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CertificateResponse>>> getMyCertificates(
            @AuthenticationPrincipal UserDetails principal) {
        List<CertificateResponse> certificates = certificateService.getMyCertificates(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(certificates));
    }

    @GetMapping("/files/{fileName}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ByteArrayResource> getCertificateFile(
            @PathVariable String fileName,
            @RequestParam(name = "download", defaultValue = "false") boolean download,
            @AuthenticationPrincipal UserDetails principal) {
        try {
            byte[] data = certificateService.readOwnedCertificateFile(principal.getUsername(), fileName);
            ByteArrayResource resource = new ByteArrayResource(data);
            String disposition = download ? "attachment" : "inline";
            return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/svg+xml"))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + fileName + "\"")
                .body(resource);
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/public/{token}")
    public ResponseEntity<ApiResponse<PublicCertificateResponse>> getPublicCertificate(
            @PathVariable String token) {
        Certificate certificate = certificateService.getCertificateByToken(token);
        return ResponseEntity.ok(ApiResponse.ok(PublicCertificateResponse.from(certificate)));
    }

    @GetMapping("/public/{token}/file")
    public ResponseEntity<ByteArrayResource> getPublicCertificateFile(@PathVariable String token) {
        try {
            byte[] data = certificateService.readPublicCertificateFile(token);
            ByteArrayResource resource = new ByteArrayResource(data);
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("image/svg+xml"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"certificate.svg\"")
                    .body(resource);
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/public/{token}/download")
    public ResponseEntity<ByteArrayResource> downloadPublicCertificate(@PathVariable String token) {
        try {
            byte[] data = certificateService.readPublicCertificateFile(token);
            ByteArrayResource resource = new ByteArrayResource(data);
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("image/svg+xml"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"certificate.svg\"")
                    .body(resource);
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
