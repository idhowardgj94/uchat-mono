CREATE TABLE IF NOT EXISTS subscriptions (
       id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
       channels_id bigint,
       user_ID BIGINT,
       created_on TIMESTAMP,
       updated_at TIMESTAMP
);
