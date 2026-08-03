--liquibase formatted sql

--changeset mickey:001-baseline splitStatements:true
CREATE TABLE IF NOT EXISTS activity_audit_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    action_category VARCHAR(255), action_type VARCHAR(255), chat_id BIGINT,
    created_at TIMESTAMP, details VARCHAR(1000), user_id BIGINT, username VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS chat_info (
    chat_id BIGINT NOT NULL PRIMARY KEY, admin_ok BOOLEAN, last_warning_at TIMESTAMP
);
CREATE TABLE IF NOT EXISTS chat_members (
    user_id BIGINT NOT NULL PRIMARY KEY, banned BOOLEAN, first_name VARCHAR(255),
    joined_at TIMESTAMP, last_message_at TIMESTAMP, muted BOOLEAN, muted_until TIMESTAMP,
    username VARCHAR(255), warn_count INTEGER
);
CREATE TABLE IF NOT EXISTS economy_accounts (
    user_id BIGINT NOT NULL PRIMARY KEY, balance BIGINT, created_at TIMESTAMP,
    daily_streak INTEGER, last_daily_at TIMESTAMP, total_earned BIGINT, updated_at TIMESTAMP
);
CREATE TABLE IF NOT EXISTS poll_records (
    poll_id VARCHAR(255) NOT NULL PRIMARY KEY, chat_id BIGINT, closed BOOLEAN,
    created_at TIMESTAMP, question VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS punishment_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT, admin_id BIGINT, created_at TIMESTAMP,
    reason VARCHAR(255), type VARCHAR(255), user_id BIGINT, username VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS reports (
    id INTEGER PRIMARY KEY AUTOINCREMENT, chat_id BIGINT, chat_title VARCHAR(255),
    chat_username VARCHAR(255), created_at TIMESTAMP, reason VARCHAR(255), reporter_id BIGINT,
    reporter_username VARCHAR(255), resolved_at TIMESTAMP, resolved_by_id BIGINT,
    resolved_by_username VARCHAR(255), status VARCHAR(255), target_id BIGINT,
    target_username VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS scheduled_broadcasts (
    id INTEGER PRIMARY KEY AUTOINCREMENT, chat_id BIGINT, send_at TIMESTAMP,
    sent BOOLEAN, text VARCHAR(3900)
);
CREATE TABLE IF NOT EXISTS warns (
    id INTEGER PRIMARY KEY AUTOINCREMENT, active BOOLEAN NOT NULL, admin_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL, reason VARCHAR(255) NOT NULL, user_id BIGINT NOT NULL
);
CREATE TABLE IF NOT EXISTS group_member_states (
    chat_id BIGINT NOT NULL, user_id BIGINT NOT NULL, banned BOOLEAN NOT NULL,
    first_name VARCHAR(150), joined_at TIMESTAMP, last_message_at TIMESTAMP,
    muted BOOLEAN NOT NULL, muted_until TIMESTAMP, username VARCHAR(100),
    warn_count INTEGER NOT NULL, PRIMARY KEY (chat_id, user_id)
);
CREATE TABLE IF NOT EXISTS managed_chat_settings (
    chat_id BIGINT NOT NULL PRIMARY KEY, active BOOLEAN NOT NULL, audience VARCHAR(30) NOT NULL,
    broadcasts_enabled BOOLEAN NOT NULL, moderation_enabled BOOLEAN NOT NULL, specialty_id BIGINT,
    title VARCHAR(250), updated_at TIMESTAMP NOT NULL, username VARCHAR(100),
    welcome_enabled BOOLEAN NOT NULL
);
CREATE TABLE IF NOT EXISTS ukd_specialties (
    id INTEGER PRIMARY KEY AUTOINCREMENT, active BOOLEAN NOT NULL,
    catalog_key VARCHAR(100) NOT NULL UNIQUE, code VARCHAR(20), display_order INTEGER NOT NULL,
    education_level VARCHAR(30) NOT NULL, educational_program VARCHAR(300),
    full_time_tuition BIGINT, name VARCHAR(250) NOT NULL, part_time_tuition BIGINT,
    source_url VARCHAR(300) NOT NULL, source_year INTEGER NOT NULL, tuition_note VARCHAR(600),
    tuition_period VARCHAR(40) NOT NULL, updated_at TIMESTAMP NOT NULL
);
