CREATE TABLE IF NOT EXISTS teams (
       uuid uuid DEFAULT gen_random_uuid() PRIMARY KEY,
       name VARCHAR(255) NOT NULL
);
