-- V7: Doubt module tables (doubts, doubt_answers)

CREATE TABLE doubts
(
    id         BIGSERIAL   PRIMARY KEY,
    learner_id BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    problem_id BIGINT      NOT NULL REFERENCES problems (id) ON DELETE CASCADE,
    question   TEXT        NOT NULL,
    status     VARCHAR(20) NOT NULL
                   CHECK (status IN ('OPEN', 'RESOLVED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_doubts_learner_id ON doubts (learner_id);
CREATE INDEX idx_doubts_problem_id ON doubts (problem_id);
CREATE INDEX idx_doubts_status ON doubts (status);
CREATE INDEX idx_doubts_created_at ON doubts (created_at DESC);

CREATE TRIGGER trg_doubts_updated_at
    BEFORE UPDATE ON doubts
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TABLE doubt_answers
(
    id         BIGSERIAL   PRIMARY KEY,
    doubt_id   BIGINT      NOT NULL REFERENCES doubts (id) ON DELETE CASCADE,
    tutor_id   BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    answer     TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_doubt_answers_doubt_id ON doubt_answers (doubt_id);
CREATE INDEX idx_doubt_answers_tutor_id ON doubt_answers (tutor_id);