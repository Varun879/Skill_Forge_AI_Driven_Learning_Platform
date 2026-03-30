-- V8: Practice system tables
-- Covers coding practice, programming MCQs, aptitude MCQs, user attempts, sessions, and mastery analytics.

CREATE TABLE practice_questions
(
    id                            BIGSERIAL    PRIMARY KEY,
    question_type                 VARCHAR(30)  NOT NULL
                                      CHECK (question_type IN ('CODING', 'PROGRAMMING_MCQ', 'APTITUDE_MCQ')),
    title                         VARCHAR(255) NOT NULL,
    prompt                        TEXT         NOT NULL,
    difficulty_level              VARCHAR(20)  NOT NULL
                                      CHECK (difficulty_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    topic                         VARCHAR(120) NOT NULL,
    estimated_solve_time_minutes  INT          NOT NULL CHECK (estimated_solve_time_minutes > 0),
    success_rate                  NUMERIC(5,2) NOT NULL DEFAULT 0 CHECK (success_rate >= 0 AND success_rate <= 100),
    is_active                     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at                    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at                    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_practice_questions_type_difficulty_topic
    ON practice_questions (question_type, difficulty_level, topic);
CREATE INDEX idx_practice_questions_topic_success_rate
    ON practice_questions (topic, success_rate DESC);
CREATE INDEX idx_practice_questions_is_active_created_at
    ON practice_questions (is_active, created_at DESC);

CREATE TRIGGER trg_practice_questions_updated_at
    BEFORE UPDATE ON practice_questions
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TABLE practice_question_tags
(
    id          BIGSERIAL   PRIMARY KEY,
    question_id BIGINT      NOT NULL REFERENCES practice_questions (id) ON DELETE CASCADE,
    tag         VARCHAR(50) NOT NULL,
    CONSTRAINT uq_practice_question_tag UNIQUE (question_id, tag)
);

CREATE INDEX idx_practice_question_tags_tag ON practice_question_tags (tag);
CREATE INDEX idx_practice_question_tags_question_id ON practice_question_tags (question_id);

CREATE TABLE programming_mcqs
(
    question_id  BIGINT PRIMARY KEY REFERENCES practice_questions (id) ON DELETE CASCADE,
    explanation  TEXT
);

CREATE TABLE aptitude_mcqs
(
    question_id  BIGINT PRIMARY KEY REFERENCES practice_questions (id) ON DELETE CASCADE,
    explanation  TEXT
);

CREATE TABLE mcq_options
(
    id             BIGSERIAL    PRIMARY KEY,
    question_id    BIGINT       NOT NULL REFERENCES practice_questions (id) ON DELETE CASCADE,
    option_text    VARCHAR(500) NOT NULL,
    display_order  INT          NOT NULL CHECK (display_order > 0),
    is_correct     BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_mcq_option_order UNIQUE (question_id, display_order)
);

CREATE INDEX idx_mcq_options_question_id ON mcq_options (question_id);

CREATE TABLE practice_sessions
(
    id                          BIGSERIAL   PRIMARY KEY,
    user_id                     BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    session_type                VARCHAR(30) NOT NULL
                                   CHECK (session_type IN ('CODING', 'PROGRAMMING_MCQ', 'APTITUDE_MCQ', 'MIXED')),
    started_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ended_at                    TIMESTAMPTZ,
    total_time_taken_seconds    INT          CHECK (total_time_taken_seconds IS NULL OR total_time_taken_seconds >= 0),
    accuracy_rate               NUMERIC(5,2) CHECK (accuracy_rate IS NULL OR (accuracy_rate >= 0 AND accuracy_rate <= 100)),
    recommended_question_id     BIGINT REFERENCES practice_questions (id) ON DELETE SET NULL,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_practice_sessions_user_started_at
    ON practice_sessions (user_id, started_at DESC);
CREATE INDEX idx_practice_sessions_user_type_started_at
    ON practice_sessions (user_id, session_type, started_at DESC);

CREATE TRIGGER trg_practice_sessions_updated_at
    BEFORE UPDATE ON practice_sessions
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TABLE user_answers
(
    id                   BIGSERIAL    PRIMARY KEY,
    user_id              BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    session_id           BIGINT       REFERENCES practice_sessions (id) ON DELETE SET NULL,
    question_id          BIGINT       NOT NULL REFERENCES practice_questions (id) ON DELETE CASCADE,
    selected_option_id   BIGINT       REFERENCES mcq_options (id) ON DELETE SET NULL,
    coding_answer        TEXT,
    is_correct           BOOLEAN      NOT NULL,
    time_taken_seconds   INT          NOT NULL CHECK (time_taken_seconds > 0),
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_answers_user_created_at
    ON user_answers (user_id, created_at DESC);
CREATE INDEX idx_user_answers_session_id
    ON user_answers (session_id);
CREATE INDEX idx_user_answers_question_id
    ON user_answers (question_id);
CREATE INDEX idx_user_answers_user_question_created_at
    ON user_answers (user_id, question_id, created_at DESC);
CREATE INDEX idx_user_answers_user_correct_created_at
    ON user_answers (user_id, is_correct, created_at DESC);

CREATE TABLE practice_stats
(
    id                         BIGSERIAL    PRIMARY KEY,
    user_id                    BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    question_type              VARCHAR(30)  NOT NULL
                                   CHECK (question_type IN ('CODING', 'PROGRAMMING_MCQ', 'APTITUDE_MCQ')),
    total_attempted            INT          NOT NULL DEFAULT 0 CHECK (total_attempted >= 0),
    total_correct              INT          NOT NULL DEFAULT 0 CHECK (total_correct >= 0),
    total_time_taken_seconds   BIGINT       NOT NULL DEFAULT 0 CHECK (total_time_taken_seconds >= 0),
    accuracy_rate              NUMERIC(5,2) NOT NULL DEFAULT 0 CHECK (accuracy_rate >= 0 AND accuracy_rate <= 100),
    last_answered_at           TIMESTAMPTZ,
    created_at                 TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at                 TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_practice_stats_user_type UNIQUE (user_id, question_type)
);

CREATE INDEX idx_practice_stats_user_id ON practice_stats (user_id);
CREATE INDEX idx_practice_stats_accuracy_rate ON practice_stats (accuracy_rate DESC);

CREATE TRIGGER trg_practice_stats_updated_at
    BEFORE UPDATE ON practice_stats
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TABLE topic_mastery
(
    id                              BIGSERIAL    PRIMARY KEY,
    user_id                         BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    topic                           VARCHAR(120) NOT NULL,
    question_type                   VARCHAR(30)  NOT NULL
                                        CHECK (question_type IN ('CODING', 'PROGRAMMING_MCQ', 'APTITUDE_MCQ')),
    attempted_count                 INT          NOT NULL DEFAULT 0 CHECK (attempted_count >= 0),
    correct_count                   INT          NOT NULL DEFAULT 0 CHECK (correct_count >= 0),
    mastery_score                   NUMERIC(5,2) NOT NULL DEFAULT 0 CHECK (mastery_score >= 0 AND mastery_score <= 100),
    avg_time_taken_seconds          INT          NOT NULL DEFAULT 0 CHECK (avg_time_taken_seconds >= 0),
    recommended_question_id         BIGINT       REFERENCES practice_questions (id) ON DELETE SET NULL,
    created_at                      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at                      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_topic_mastery_user_topic_type UNIQUE (user_id, topic, question_type)
);

CREATE INDEX idx_topic_mastery_user_mastery
    ON topic_mastery (user_id, mastery_score ASC);
CREATE INDEX idx_topic_mastery_topic_type
    ON topic_mastery (topic, question_type);

CREATE TRIGGER trg_topic_mastery_updated_at
    BEFORE UPDATE ON topic_mastery
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
