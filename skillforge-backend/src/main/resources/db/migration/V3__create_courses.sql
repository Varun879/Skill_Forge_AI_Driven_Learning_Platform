-- V3: Course module tables (courses, course_tags, course_enrollments)

-- ── Courses ───────────────────────────────────────────────────────────────
CREATE TABLE courses
(
    id               BIGSERIAL     PRIMARY KEY,
    title            VARCHAR(255)  NOT NULL,
    description      TEXT,
    tutor_id         BIGINT        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    status           VARCHAR(20)   NOT NULL DEFAULT 'DRAFT'
                         CHECK (status IN ('DRAFT', 'PUBLISHED')),
    difficulty_level VARCHAR(20)   NOT NULL
                         CHECK (difficulty_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    price            NUMERIC(10,2) NOT NULL DEFAULT 0.00
                         CHECK (price >= 0),
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_courses_tutor_id ON courses (tutor_id);
CREATE INDEX idx_courses_status   ON courses (status);

CREATE TRIGGER trg_courses_updated_at
    BEFORE UPDATE ON courses
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- ── Course tags ───────────────────────────────────────────────────────────
CREATE TABLE course_tags
(
    id        BIGSERIAL    PRIMARY KEY,
    course_id BIGINT       NOT NULL REFERENCES courses (id) ON DELETE CASCADE,
    name      VARCHAR(50)  NOT NULL
);

CREATE INDEX idx_course_tags_course_id ON course_tags (course_id);

-- ── Course enrollments ────────────────────────────────────────────────────
CREATE TABLE course_enrollments
(
    id          BIGSERIAL   PRIMARY KEY,
    course_id   BIGINT      NOT NULL REFERENCES courses (id) ON DELETE CASCADE,
    learner_id  BIGINT      NOT NULL REFERENCES users (id)   ON DELETE CASCADE,
    enrolled_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_enrollment_course_learner UNIQUE (course_id, learner_id)
);

CREATE INDEX idx_enrollments_course_id  ON course_enrollments (course_id);
CREATE INDEX idx_enrollments_learner_id ON course_enrollments (learner_id);
