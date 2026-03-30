package com.skillforge.domain.user.dto;

import com.skillforge.common.enums.Role;

import jakarta.validation.constraints.NotBlank;

public class GoogleAuthRequest {

    /**
     * Google ID token obtained by the frontend via the Google Sign-In SDK.
     * Verified against Google's public keys server-side.
     */
    @NotBlank(message = "Google ID token is required")
    private String idToken;

    /**
     * Required only on first-time sign-up so we know what role to assign.
     * For returning users the existing role is preserved.
     * Defaults to LEARNER if omitted.
     */
    private Role role;

    public String getIdToken() { return idToken; }
    public void setIdToken(String v) { this.idToken = v; }

    public Role getRole() { return role; }
    public void setRole(Role v) { this.role = v; }
}
