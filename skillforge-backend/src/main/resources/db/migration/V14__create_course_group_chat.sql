CREATE TABLE IF NOT EXISTS course_group_messages (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    sender_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    sender_role VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    message_time TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_course_group_messages_course_time
    ON course_group_messages(course_id, message_time DESC);

CREATE INDEX IF NOT EXISTS idx_course_group_messages_sender
    ON course_group_messages(sender_id);
