-- V4: Problem module tables (problems, problem_tags, test_cases)

CREATE TABLE problems
(
    id               BIGSERIAL    PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    description      TEXT         NOT NULL,
    difficulty_level VARCHAR(20)  NOT NULL
                         CHECK (difficulty_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    constraints_text TEXT,
    tutor_id         BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_problems_tutor_id ON problems (tutor_id);
CREATE INDEX idx_problems_difficulty_level ON problems (difficulty_level);

CREATE TRIGGER trg_problems_updated_at
    BEFORE UPDATE ON problems
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TABLE problem_tags
(
    id         BIGSERIAL   PRIMARY KEY,
    problem_id BIGINT      NOT NULL REFERENCES problems (id) ON DELETE CASCADE,
    name       VARCHAR(50) NOT NULL
);

CREATE INDEX idx_problem_tags_problem_id ON problem_tags (problem_id);

CREATE TABLE test_cases
(
    id              BIGSERIAL PRIMARY KEY,
    problem_id      BIGINT    NOT NULL REFERENCES problems (id) ON DELETE CASCADE,
    input_data      TEXT      NOT NULL,
    expected_output TEXT      NOT NULL,
    explanation     TEXT,
    is_sample       BOOLEAN   NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_test_cases_problem_id ON test_cases (problem_id);
