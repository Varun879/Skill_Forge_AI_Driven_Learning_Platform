CREATE TABLE IF NOT EXISTS course_modules (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    video_url VARCHAR(500),
    order_index INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_course_modules_course_id ON course_modules(course_id);
CREATE INDEX IF NOT EXISTS idx_course_modules_course_order ON course_modules(course_id, order_index);

CREATE TABLE IF NOT EXISTS learner_module_progress (
    id BIGSERIAL PRIMARY KEY,
    module_id BIGINT NOT NULL REFERENCES course_modules(id) ON DELETE CASCADE,
    learner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    completed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_learner_module_progress UNIQUE(module_id, learner_id)
);

CREATE INDEX IF NOT EXISTS idx_learner_module_progress_learner ON learner_module_progress(learner_id);
CREATE INDEX IF NOT EXISTS idx_learner_module_progress_module ON learner_module_progress(module_id);

CREATE TABLE IF NOT EXISTS course_exam_attempts (
    id BIGSERIAL PRIMARY KEY,
    course_exam_id BIGINT NOT NULL REFERENCES course_exams(id) ON DELETE CASCADE,
    learner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    started_at TIMESTAMP NOT NULL DEFAULT NOW(),
    submitted_at TIMESTAMP,
    score NUMERIC(5,2),
    status VARCHAR(20) NOT NULL DEFAULT 'STARTED'
);

CREATE INDEX IF NOT EXISTS idx_course_exam_attempts_exam ON course_exam_attempts(course_exam_id);
CREATE INDEX IF NOT EXISTS idx_course_exam_attempts_learner ON course_exam_attempts(learner_id);

CREATE TABLE IF NOT EXISTS course_exam_attempt_answers (
    id BIGSERIAL PRIMARY KEY,
    attempt_id BIGINT NOT NULL REFERENCES course_exam_attempts(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES practice_questions(id) ON DELETE CASCADE,
    selected_option_id BIGINT REFERENCES mcq_options(id) ON DELETE SET NULL,
    is_correct BOOLEAN,
    CONSTRAINT uq_course_exam_attempt_question UNIQUE(attempt_id, question_id)
);

CREATE INDEX IF NOT EXISTS idx_course_exam_attempt_answers_attempt ON course_exam_attempt_answers(attempt_id);

ALTER TABLE certificates
    ADD COLUMN IF NOT EXISTS public_token VARCHAR(64);

CREATE UNIQUE INDEX IF NOT EXISTS uq_certificates_public_token ON certificates(public_token);
