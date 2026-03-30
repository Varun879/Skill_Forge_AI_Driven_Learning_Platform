package com.skillforge.domain.user.dto;

import com.skillforge.common.enums.Role;

public class AuthResponse {

    private String      accessToken;
    private String      refreshToken;
    private String      tokenType;
    private Long        expiresIn;
    private String      role;
    private Long        userId;
    private String      name;
    private UserSummary user;

    private AuthResponse(Builder b) {
        this.accessToken  = b.accessToken;
        this.refreshToken = b.refreshToken;
        this.tokenType    = b.tokenType;
        this.expiresIn    = b.expiresIn;
        this.role         = b.role;
        this.userId       = b.userId;
        this.name         = b.name;
        this.user         = b.user;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String      accessToken;
        private String      refreshToken;
        private String      tokenType;
        private Long        expiresIn;
        private String      role;
        private Long        userId;
        private String      name;
        private UserSummary user;

        public Builder accessToken(String v)  { this.accessToken = v;  return this; }
        public Builder refreshToken(String v) { this.refreshToken = v; return this; }
        public Builder tokenType(String v)    { this.tokenType = v;    return this; }
        public Builder expiresIn(Long v)      { this.expiresIn = v;    return this; }
        public Builder role(String v)         { this.role = v;         return this; }
        public Builder userId(Long v)         { this.userId = v;       return this; }
        public Builder name(String v)         { this.name = v;         return this; }
        public Builder user(UserSummary v)    { this.user = v;         return this; }
        public AuthResponse build()           { return new AuthResponse(this); }
    }

    public String      getAccessToken()  { return accessToken; }
    public String      getRefreshToken() { return refreshToken; }
    public String      getTokenType()    { return tokenType; }
    public Long        getExpiresIn()    { return expiresIn; }
    public String      getRole()         { return role; }
    public Long        getUserId()       { return userId; }
    public String      getName()         { return name; }
    public UserSummary getUser()         { return user; }

    // ── Nested UserSummary ────────────────────────────────────────────────

    public static class UserSummary {
        private Long   id;
        private String email;
        private String username;
        private String firstName;
        private String lastName;
        private String avatarUrl;
        private Role   role;

        private UserSummary(Builder b) {
            this.id        = b.id;
            this.email     = b.email;
            this.username  = b.username;
            this.firstName = b.firstName;
            this.lastName  = b.lastName;
            this.avatarUrl = b.avatarUrl;
            this.role      = b.role;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long   id;
            private String email;
            private String username;
            private String firstName;
            private String lastName;
            private String avatarUrl;
            private Role   role;

            public Builder id(Long v)         { this.id = v;        return this; }
            public Builder email(String v)    { this.email = v;     return this; }
            public Builder username(String v) { this.username = v;  return this; }
            public Builder firstName(String v){ this.firstName = v; return this; }
            public Builder lastName(String v) { this.lastName = v;  return this; }
            public Builder avatarUrl(String v){ this.avatarUrl = v; return this; }
            public Builder role(Role v)       { this.role = v;      return this; }
            public UserSummary build()        { return new UserSummary(this); }
        }

        public Long   getId()        { return id; }
        public String getEmail()     { return email; }
        public String getUsername()  { return username; }
        public String getFirstName() { return firstName; }
        public String getLastName()  { return lastName; }
        public String getAvatarUrl() { return avatarUrl; }
        public Role   getRole()      { return role; }
    }
}
