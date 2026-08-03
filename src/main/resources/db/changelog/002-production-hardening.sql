--liquibase formatted sql

--changeset mickey:002-chat-scoped-moderation splitStatements:true
ALTER TABLE punishment_logs ADD COLUMN chat_id BIGINT;
ALTER TABLE warns ADD COLUMN chat_id BIGINT;

--changeset mickey:003-broadcast-lifecycle splitStatements:true
ALTER TABLE scheduled_broadcasts ADD COLUMN status VARCHAR(30);
ALTER TABLE scheduled_broadcasts ADD COLUMN attempt_count INTEGER DEFAULT 0;
ALTER TABLE scheduled_broadcasts ADD COLUMN processing_started_at TIMESTAMP;
ALTER TABLE scheduled_broadcasts ADD COLUMN last_attempt_at TIMESTAMP;
ALTER TABLE scheduled_broadcasts ADD COLUMN next_attempt_at TIMESTAMP;
ALTER TABLE scheduled_broadcasts ADD COLUMN last_error VARCHAR(1000);
UPDATE scheduled_broadcasts
   SET status = CASE WHEN sent = 1 THEN 'SENT' ELSE 'PENDING' END
 WHERE status IS NULL;

--changeset mickey:004-query-indexes splitStatements:true
CREATE INDEX IF NOT EXISTS idx_audit_chat_created ON activity_audit_logs(chat_id, created_at);
CREATE INDEX IF NOT EXISTS idx_audit_category_created ON activity_audit_logs(action_category, created_at);
CREATE INDEX IF NOT EXISTS idx_warn_chat_user_active ON warns(chat_id, user_id, active);
CREATE INDEX IF NOT EXISTS idx_punishment_chat_user_created ON punishment_logs(chat_id, user_id, created_at);
CREATE INDEX IF NOT EXISTS idx_report_chat_status_created ON reports(chat_id, status, created_at);
CREATE INDEX IF NOT EXISTS idx_broadcast_due ON scheduled_broadcasts(sent, status, send_at, next_attempt_at);
CREATE INDEX IF NOT EXISTS idx_broadcast_chat ON scheduled_broadcasts(chat_id);
CREATE INDEX IF NOT EXISTS idx_group_member_chat_activity ON group_member_states(chat_id, last_message_at);

--changeset mickey:005-managed-study-groups splitStatements:true
CREATE TABLE IF NOT EXISTS managed_study_groups (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    category VARCHAR(30) NOT NULL,
    specialty VARCHAR(250) NOT NULL,
    course VARCHAR(100),
    invite_link VARCHAR(200) NOT NULL UNIQUE,
    note VARCHAR(500),
    active BOOLEAN NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_study_group_active_category ON managed_study_groups(active, category);
CREATE INDEX IF NOT EXISTS idx_study_group_specialty ON managed_study_groups(specialty);

--changeset mickey:006-economy-optimistic-lock splitStatements:true
ALTER TABLE economy_accounts ADD COLUMN version BIGINT DEFAULT 0;
UPDATE economy_accounts SET version = 0 WHERE version IS NULL;
