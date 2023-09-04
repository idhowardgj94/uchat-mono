CREATE TABLE IF NOT EXISTS teams_users (
       id bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
       user_id BIGINT,
       channel_id BIGINT,
       created_on TIMESTAMP,
       updated_at TIMESTAMP
);
