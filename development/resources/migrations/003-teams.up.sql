CREATE TABLE IF NOT EXISTS teams (
       id serial PRIMARY KEY,
       uuid VARCHAR(255) UNIQUE NOT NULL,
       name VARCHAR(255) NOT NULL
);
