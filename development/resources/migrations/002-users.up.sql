CREATE TABLE IF NOT EXISTS users (
       id serial PRIMARY KEY,
       username VARCHAR(255) UNIQUE NOT NULL,
       password VARCHAR(255) NOT NULL,
       email VARCHAR(255),
       name VARCHAR(255),
       created_on TIMESTAMP,
       updated_at TIMESTAMP
);

