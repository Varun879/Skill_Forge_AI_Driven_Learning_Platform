CREATE TABLE session_questions (
    id                  BIGSERIAL PRIMARY KEY,
    session_id          BIGINT NOT NULL REFERENCES practice_sessions(id) ON DELETE CASCADE,
    question_id         BIGINT NOT NULL REFERENCES practice_questions(id) ON DELETE CASCADE,
    selected_option_id  BIGINT REFERENCES mcq_options(id) ON DELETE SET NULL,
    is_correct          BOOLEAN NOT NULL,
    time_taken_seconds  INT NOT NULL CHECK (time_taken_seconds > 0),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_session_questions_session_created
    ON session_questions (session_id, created_at DESC);

CREATE INDEX idx_session_questions_question_id
    ON session_questions (question_id);
