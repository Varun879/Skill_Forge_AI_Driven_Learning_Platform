CREATE TABLE executions
(
    id                 BIGSERIAL PRIMARY KEY,
    learner_id         BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    problem_id         BIGINT      NOT NULL REFERENCES problems (id) ON DELETE CASCADE,
    submission_id      BIGINT      NULL REFERENCES submissions (id) ON DELETE SET NULL,
    mode               VARCHAR(20) NOT NULL CHECK (mode IN ('RUN', 'SUBMIT')),
    language           VARCHAR(20) NOT NULL,
    source_code        TEXT        NOT NULL,
    stdin_data         TEXT,
    stdout_data        TEXT,
    stderr_data        TEXT,
    status             VARCHAR(40) NOT NULL CHECK (
        status IN (
            'QUEUED',
            'RUNNING',
            'SUCCESS',
            'TLE',
            'MEMORY_LIMIT_EXCEEDED',
            'RUNTIME_ERROR',
            'COMPILATION_ERROR',
            'FAILED'
        )
    ),
    execution_time_ms  BIGINT,
    memory_usage_kb    BIGINT,
    cache_hit          BOOLEAN     NOT NULL DEFAULT FALSE,
    started_at         TIMESTAMPTZ,
    completed_at       TIMESTAMPTZ,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_executions_learner_id ON executions (learner_id);
CREATE INDEX idx_executions_problem_id ON executions (problem_id);
CREATE INDEX idx_executions_status ON executions (status);
CREATE INDEX idx_executions_created_at ON executions (created_at DESC);

CREATE TRIGGER trg_executions_updated_at
    BEFORE UPDATE ON executions
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TABLE execution_test_results
(
    id                 BIGSERIAL PRIMARY KEY,
    execution_id       BIGINT      NOT NULL REFERENCES executions (id) ON DELETE CASCADE,
    test_case_id       BIGINT      NULL REFERENCES test_cases (id) ON DELETE SET NULL,
    sample             BOOLEAN     NOT NULL DEFAULT FALSE,
    passed             BOOLEAN     NOT NULL DEFAULT FALSE,
    status             VARCHAR(40) NOT NULL,
    expected_output    TEXT,
    actual_output      TEXT,
    stdout_data        TEXT,
    stderr_data        TEXT,
    execution_time_ms  BIGINT,
    memory_usage_kb    BIGINT,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_execution_test_results_execution_id ON execution_test_results (execution_id);
CREATE INDEX idx_execution_test_results_test_case_id ON execution_test_results (test_case_id);
