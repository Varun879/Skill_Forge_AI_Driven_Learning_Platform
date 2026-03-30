ALTER TABLE courses
    ADD COLUMN IF NOT EXISTS youtube_video_url VARCHAR(500);

CREATE TABLE IF NOT EXISTS course_exams (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    tutor_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    duration_minutes INTEGER NOT NULL,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_course_exams_course_id ON course_exams(course_id);
CREATE INDEX IF NOT EXISTS idx_course_exams_tutor_id ON course_exams(tutor_id);
CREATE INDEX IF NOT EXISTS idx_course_exams_published ON course_exams(is_published);

CREATE TABLE IF NOT EXISTS course_exam_questions (
    id BIGSERIAL PRIMARY KEY,
    course_exam_id BIGINT NOT NULL REFERENCES course_exams(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES practice_questions(id) ON DELETE CASCADE,
    question_order INTEGER NOT NULL,
    CONSTRAINT uq_course_exam_question UNIQUE(course_exam_id, question_id)
);

CREATE INDEX IF NOT EXISTS idx_course_exam_questions_exam ON course_exam_questions(course_exam_id);
CREATE INDEX IF NOT EXISTS idx_course_exam_questions_question ON course_exam_questions(question_id);
