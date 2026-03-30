package com.skillforge.domain.user.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "refresh_tokens",
    indexes = {
        @Index(name = "idx_refresh_tokens_user_id",    columnList = "user_id"),
        @Index(name = "idx_refresh_tokens_token_hash", columnList = "token_hash")
    }
)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * SHA-256 hash of the raw opaque token sent to the client.
     * The plain value is never persisted — a DB breach cannot yield replayable tokens.
     */
    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_revoked", nullable = false)
    private boolean revoked = false;

    /** Optional user-agent for session audit purposes. */
    @Column(name = "device_info", length = 512)
    private String deviceInfo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected RefreshToken() {}

    private RefreshToken(Builder b) {
        this.user       = b.user;
        this.tokenHash  = b.tokenHash;
        this.expiresAt  = b.expiresAt;
        this.revoked    = b.revoked;
        this.deviceInfo = b.deviceInfo;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private User          user;
        private String        tokenHash;
        private LocalDateTime expiresAt;
        private boolean       revoked    = false;
        private String        deviceInfo;

        public Builder user(User v)               { this.user = v;       return this; }
        public Builder tokenHash(String v)        { this.tokenHash = v;  return this; }
        public Builder expiresAt(LocalDateTime v) { this.expiresAt = v;  return this; }
        public Builder revoked(boolean v)         { this.revoked = v;    return this; }
        public Builder deviceInfo(String v)       { this.deviceInfo = v; return this; }
        public RefreshToken build()               { return new RefreshToken(this); }
    }

    public Long          getId()         { return id; }
    public User          getUser()       { return user; }
    public String        getTokenHash()  { return tokenHash; }
    public LocalDateTime getExpiresAt()  { return expiresAt; }
    public boolean       isRevoked()     { return revoked; }
    public String        getDeviceInfo() { return deviceInfo; }
    public LocalDateTime getCreatedAt()  { return createdAt; }

    public void setId(Long id)                 { this.id = id; }
    public void setUser(User v)                { this.user = v; }
    public void setTokenHash(String v)         { this.tokenHash = v; }
    public void setExpiresAt(LocalDateTime dt) { this.expiresAt = dt; }
    public void setRevoked(boolean v)          { this.revoked = v; }
    public void setDeviceInfo(String v)        { this.deviceInfo = v; }
    public void setCreatedAt(LocalDateTime dt) { this.createdAt = dt; }
}
