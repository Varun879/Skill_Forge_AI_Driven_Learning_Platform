-- V1: Core users table
-- Supports email/password, Google OAuth, and OTP-only accounts.

CREATE TABLE users
(
    id            BIGSERIAL    PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),                        -- NULL for OAuth / OTP-only accounts
    role          VARCHAR(20)  NOT NULL
                      CHECK (role IN ('LEARNER', 'TUTOR')),
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    username      VARCHAR(100) NOT NULL UNIQUE,
    avatar_url    VARCHAR(500),
    bio           TEXT,
    google_id     VARCHAR(255) UNIQUE,                 -- populated on Google OAuth link
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email    ON users (email);
CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_role     ON users (role);

-- Trigger to keep updated_at current on every row update
CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
