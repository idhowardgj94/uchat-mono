CREATE TABLE IF NOT EXISTS groups_channels (
       id bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
       user_id bigint,
       group_id bigint,
       group_name varchar(255),
       channel_id bigint,
       channel_name varchar(255)
);
