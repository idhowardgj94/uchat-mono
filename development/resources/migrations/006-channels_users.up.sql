CREATE TABLE IF NOT EXISTS channels_users (
       id bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
       channels_id bigint,
       users_id bigint
);
