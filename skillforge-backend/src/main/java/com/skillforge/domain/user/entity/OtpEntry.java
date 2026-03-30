package com.skillforge.domain.user.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "otp_entries",
    indexes = {
        @Index(name = "idx_otp_email",      columnList = "email"),
        @Index(name = "idx_otp_expires_at", columnList = "expires_at")
    }
)
public class OtpEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    /** Stored plain-text; brute-force mitigated by attempt cap (5) and short TTL. */
    @Column(name = "otp_code", nullable = false, length = 10)
    private String otpCode;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_used", nullable = false)
    private boolean used = false;

    /** Invalidated after 5 wrong guesses to block brute-force enumeration. */
    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected OtpEntry() {}

    private OtpEntry(Builder b) {
        this.email        = b.email;
        this.otpCode      = b.otpCode;
        this.expiresAt    = b.expiresAt;
        this.used         = b.used;
        this.attemptCount = b.attemptCount;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String        email;
        private String        otpCode;
        private LocalDateTime expiresAt;
        private boolean       used         = false;
        private int           attemptCount = 0;

        public Builder email(String v)            { this.email = v;        return this; }
        public Builder otpCode(String v)          { this.otpCode = v;      return this; }
        public Builder expiresAt(LocalDateTime v) { this.expiresAt = v;    return this; }
        public Builder used(boolean v)            { this.used = v;         return this; }
        public Builder attemptCount(int v)        { this.attemptCount = v; return this; }
        public OtpEntry build()                   { return new OtpEntry(this); }
    }

    public Long          getId()           { return id; }
    public String        getEmail()        { return email; }
    public String        getOtpCode()      { return otpCode; }
    public LocalDateTime getExpiresAt()    { return expiresAt; }
    public boolean       isUsed()          { return used; }
    public int           getAttemptCount() { return attemptCount; }
    public LocalDateTime getCreatedAt()    { return createdAt; }

    public void setId(Long id)                 { this.id = id; }
    public void setEmail(String v)             { this.email = v; }
    public void setOtpCode(String v)           { this.otpCode = v; }
    public void setExpiresAt(LocalDateTime dt) { this.expiresAt = dt; }
    public void setUsed(boolean v)             { this.used = v; }
    public void setAttemptCount(int v)         { this.attemptCount = v; }
    public void setCreatedAt(LocalDateTime dt) { this.createdAt = dt; }
}
