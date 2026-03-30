-- V10: user_topic_performance table
-- Tracks per-user, per-category accuracy and timing for the AI-powered
-- practice recommendation system. This table is additive and does NOT
-- replace or alter the existing topic_mastery table.

CREATE TABLE user_topic_performance (
    id                       BIGSERIAL    PRIMARY KEY,
    user_id                  BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category                 VARCHAR(120) NOT NULL,
    question_type            VARCHAR(30)  NOT NULL CHECK (question_type IN ('CODING','PROGRAMMING_MCQ','APTITUDE_MCQ')),
    number_of_attempts       INTEGER      NOT NULL DEFAULT 0,
    correct_attempts         INTEGER      NOT NULL DEFAULT 0,
    accuracy                 DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    average_solve_time_seconds DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    recent_wrong_streak      INTEGER      NOT NULL DEFAULT 0,
    created_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_topic_perf_user_category_type
        UNIQUE (user_id, category, question_type)
);

CREATE INDEX idx_utp_user_type       ON user_topic_performance (user_id, question_type);
CREATE INDEX idx_utp_category_type   ON user_topic_performance (category, question_type);

CREATE TRIGGER trg_user_topic_performance_updated_at
    BEFORE UPDATE ON user_topic_performance
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
