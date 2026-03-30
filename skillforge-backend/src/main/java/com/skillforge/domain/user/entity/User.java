package com.skillforge.domain.user.entity;

import com.skillforge.common.enums.Role;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_users_email",    columnList = "email"),
        @Index(name = "idx_users_username", columnList = "username"),
        @Index(name = "idx_users_role",     columnList = "role")
    }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    /** Nullable for OAuth-only / OTP-only accounts that have never set a password. */
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(unique = true, nullable = false, length = 100)
    private String username;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    /** Populated when the account is linked to a Google identity. */
    @Column(name = "google_id", unique = true)
    private String googleId;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected User() {}

    private User(Builder b) {
        this.email        = b.email;
        this.passwordHash = b.passwordHash;
        this.role         = b.role;
        this.firstName    = b.firstName;
        this.lastName     = b.lastName;
        this.username     = b.username;
        this.avatarUrl    = b.avatarUrl;
        this.bio          = b.bio;
        this.googleId     = b.googleId;
        this.isActive     = b.isActive;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String  email;
        private String  passwordHash;
        private Role    role;
        private String  firstName;
        private String  lastName;
        private String  username;
        private String  avatarUrl;
        private String  bio;
        private String  googleId;
        private boolean isActive = true;

        public Builder email(String v)        { this.email = v;        return this; }
        public Builder passwordHash(String v) { this.passwordHash = v; return this; }
        public Builder role(Role v)           { this.role = v;         return this; }
        public Builder firstName(String v)    { this.firstName = v;    return this; }
        public Builder lastName(String v)     { this.lastName = v;     return this; }
        public Builder username(String v)     { this.username = v;     return this; }
        public Builder avatarUrl(String v)    { this.avatarUrl = v;    return this; }
        public Builder bio(String v)          { this.bio = v;          return this; }
        public Builder googleId(String v)     { this.googleId = v;     return this; }
        public Builder isActive(boolean v)    { this.isActive = v;     return this; }
        public User build()                   { return new User(this); }
    }

    public Long          getId()           { return id; }
    public String        getEmail()        { return email; }
    public String        getPasswordHash() { return passwordHash; }
    public Role          getRole()         { return role; }
    public String        getFirstName()    { return firstName; }
    public String        getLastName()     { return lastName; }
    public String        getUsername()     { return username; }
    public String        getAvatarUrl()    { return avatarUrl; }
    public String        getBio()          { return bio; }
    public String        getGoogleId()     { return googleId; }
    public boolean       isActive()        { return isActive; }
    public LocalDateTime getCreatedAt()    { return createdAt; }
    public LocalDateTime getUpdatedAt()    { return updatedAt; }

    public void setId(Long id)                 { this.id = id; }
    public void setEmail(String v)             { this.email = v; }
    public void setPasswordHash(String v)      { this.passwordHash = v; }
    public void setRole(Role v)                { this.role = v; }
    public void setFirstName(String v)         { this.firstName = v; }
    public void setLastName(String v)          { this.lastName = v; }
    public void setUsername(String v)          { this.username = v; }
    public void setAvatarUrl(String v)         { this.avatarUrl = v; }
    public void setBio(String v)               { this.bio = v; }
    public void setGoogleId(String v)          { this.googleId = v; }
    public void setActive(boolean v)           { this.isActive = v; }
    public void setCreatedAt(LocalDateTime dt) { this.createdAt = dt; }
    public void setUpdatedAt(LocalDateTime dt) { this.updatedAt = dt; }
}
