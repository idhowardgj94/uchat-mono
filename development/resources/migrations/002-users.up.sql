CREATE OR REPLACE FUNCTION updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
 END;
 $$ language 'plpgsql';


CREATE TABLE IF NOT EXISTS users (
       id bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
       username VARCHAR(255) UNIQUE NOT NULL,
       password VARCHAR(255) NOT NULL,
       email VARCHAR(255) UNIQUE,
       name VARCHAR(255),
       created_on TIMESTAMP SET DEFAULT now(),
       updated_at TIMESTAMP SET DEFAULT now()
);

CREATE UNIQUE INDEX username_idx ON users (username);
CREATE TRIGGER updated_at_users BEFORE UPDATE ON users FOR EACH ROW EXECUTE PROCEDURE updated_at_column();
