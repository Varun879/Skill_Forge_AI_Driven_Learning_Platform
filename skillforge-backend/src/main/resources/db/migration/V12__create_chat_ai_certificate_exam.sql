-- V12: Chat, AI, Certificate and Exam modules (additive only)

CREATE TABLE chat_rooms
(
    id         BIGSERIAL PRIMARY KEY,
    course_id  BIGINT      NOT NULL REFERENCES courses (id) ON DELETE CASCADE,
    tutor_id   BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    student_id BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_chat_room_course_tutor_student UNIQUE (course_id, tutor_id, student_id)
);

CREATE INDEX idx_chat_rooms_course_id ON chat_rooms (course_id);
CREATE INDEX idx_chat_rooms_tutor_id ON chat_rooms (tutor_id);
CREATE INDEX idx_chat_rooms_student_id ON chat_rooms (student_id);

CREATE TABLE chat_messages
(
    id            BIGSERIAL PRIMARY KEY,
    chat_room_id  BIGINT       NOT NULL REFERENCES chat_rooms (id) ON DELETE CASCADE,
    sender_id     BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    sender_role   VARCHAR(20)  NOT NULL CHECK (sender_role IN ('TUTOR', 'STUDENT')),
    message       TEXT         NOT NULL,
    message_time  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_chat_messages_room_time ON chat_messages (chat_room_id, message_time DESC);
CREATE INDEX idx_chat_messages_sender_id ON chat_messages (sender_id);

CREATE TABLE certificates
(
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    course_id       BIGINT      NOT NULL REFERENCES courses (id) ON DELETE CASCADE,
    issued_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    certificate_url VARCHAR(500) NOT NULL,
    CONSTRAINT uq_certificate_user_course UNIQUE (user_id, course_id)
);

CREATE INDEX idx_certificates_user_id ON certificates (user_id);
CREATE INDEX idx_certificates_course_id ON certificates (course_id);

CREATE TABLE exam_sessions
(
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    start_time       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    end_time         TIMESTAMPTZ NOT NULL,
    submitted_at     TIMESTAMPTZ,
    duration_seconds INT         NOT NULL,
    score            NUMERIC(5,2),
    status           VARCHAR(20) NOT NULL DEFAULT 'STARTED'
                      CHECK (status IN ('STARTED', 'SUBMITTED', 'AUTO_SUBMITTED'))
);

CREATE INDEX idx_exam_sessions_user_id ON exam_sessions (user_id);
CREATE INDEX idx_exam_sessions_user_start_time ON exam_sessions (user_id, start_time DESC);

CREATE TABLE exam_questions
(
    id                 BIGSERIAL PRIMARY KEY,
    exam_session_id    BIGINT      NOT NULL REFERENCES exam_sessions (id) ON DELETE CASCADE,
    question_id        BIGINT      NOT NULL REFERENCES practice_questions (id) ON DELETE CASCADE,
    selected_option_id BIGINT      REFERENCES mcq_options (id) ON DELETE SET NULL,
    question_order     INT         NOT NULL,
    is_correct         BOOLEAN,
    answered_at        TIMESTAMPTZ,
    CONSTRAINT uq_exam_question_session_question UNIQUE (exam_session_id, question_id)
);

CREATE INDEX idx_exam_questions_session_id ON exam_questions (exam_session_id);
CREATE INDEX idx_exam_questions_question_id ON exam_questions (question_id);
