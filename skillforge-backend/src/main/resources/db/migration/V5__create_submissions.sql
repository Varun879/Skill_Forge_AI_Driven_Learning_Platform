-- V5: Submission module tables (submissions, submission_results, review_feedbacks)

CREATE TABLE submissions
(
    id         BIGSERIAL   PRIMARY KEY,
    learner_id BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    problem_id BIGINT      NOT NULL REFERENCES problems (id) ON DELETE CASCADE,
    language   VARCHAR(30) NOT NULL,
    source_code TEXT       NOT NULL,
    status     VARCHAR(20) NOT NULL
                   CHECK (status IN ('SUBMITTED', 'ACCEPTED', 'REJECTED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_submissions_learner_id ON submissions (learner_id);
CREATE INDEX idx_submissions_problem_id ON submissions (problem_id);
CREATE INDEX idx_submissions_created_at ON submissions (created_at DESC);

CREATE TRIGGER trg_submissions_updated_at
    BEFORE UPDATE ON submissions
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TABLE submission_results
(
    id                BIGSERIAL   PRIMARY KEY,
    submission_id     BIGINT      NOT NULL UNIQUE REFERENCES submissions (id) ON DELETE CASCADE,
    passed_test_cases INT         NOT NULL DEFAULT 0 CHECK (passed_test_cases >= 0),
    total_test_cases  INT         NOT NULL DEFAULT 0 CHECK (total_test_cases >= 0),
    score             INT         NOT NULL DEFAULT 0 CHECK (score >= 0 AND score <= 100),
    message           TEXT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_submission_results_submission_id ON submission_results (submission_id);

CREATE TABLE review_feedbacks
(
    id            BIGSERIAL   PRIMARY KEY,
    submission_id BIGINT      NOT NULL UNIQUE REFERENCES submissions (id) ON DELETE CASCADE,
    tutor_id      BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    feedback      TEXT        NOT NULL,
    reviewed_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_review_feedbacks_submission_id ON review_feedbacks (submission_id);
CREATE INDEX idx_review_feedbacks_tutor_id ON review_feedbacks (tutor_id);
