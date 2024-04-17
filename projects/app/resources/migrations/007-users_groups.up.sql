CREATE TABLE IF NOT EXISTS users_groups (
       id bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
       name varchar(255)
);
