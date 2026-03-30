-- V2: Auth support tables (OTP entries and Refresh tokens)

-- ── OTP entries ───────────────────────────────────────────────────────────
-- Stores short-lived one-time passwords sent by email.
-- Rows are purged nightly by the TokenCleanupScheduler.
CREATE TABLE otp_entries
(
    id            BIGSERIAL    PRIMARY KEY,
    email         VARCHAR(255) NOT NULL,
    otp_code      VARCHAR(10)  NOT NULL,
    expires_at    TIMESTAMPTZ  NOT NULL,
    is_used       BOOLEAN      NOT NULL DEFAULT FALSE,
    attempt_count INT          NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_otp_email      ON otp_entries (email);
CREATE INDEX idx_otp_expires_at ON otp_entries (expires_at);

-- ── Refresh tokens ────────────────────────────────────────────────────────
-- Stores the SHA-256 hash of the opaque refresh token issued to the client.
-- The raw token is never persisted — only its hash — so a DB breach does not
-- expose tokens that can be replayed.
-- Rotation strategy: each use revokes the old token and issues a fresh pair.
CREATE TABLE refresh_tokens
(
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ  NOT NULL,
    is_revoked  BOOLEAN      NOT NULL DEFAULT FALSE,
    device_info VARCHAR(512),                          -- optional user-agent for audit
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_user_id    ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens (token_hash);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
