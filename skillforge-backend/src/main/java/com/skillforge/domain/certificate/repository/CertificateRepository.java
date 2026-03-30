package com.skillforge.domain.certificate.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillforge.domain.certificate.entity.Certificate;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    List<Certificate> findByUserIdOrderByIssuedAtDesc(Long userId);

    Optional<Certificate> findByUserIdAndCourseId(Long userId, Long courseId);

    Optional<Certificate> findByUserIdAndCertificateUrl(Long userId, String certificateUrl);

    Optional<Certificate> findByPublicToken(String publicToken);
}
