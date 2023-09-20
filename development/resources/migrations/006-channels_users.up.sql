CREATE TABLE IF NOT EXISTS channels_users (
       id bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
       channel_uuid uuid,
       username varchar(255)
);
-- TODO: create index for username
