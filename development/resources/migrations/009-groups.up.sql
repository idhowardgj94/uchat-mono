CREATE TABLE IF NOT EXISTS groups (
       id bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
       name VARCHAR(255),
       created_at TIMESTAMP DEFAULT now(),
       updated_at TIMESTAMP DEFAULT now()
);

CREATE TRIGGER updated_at_groups BEFORE UPDATE ON groups FOR EACH ROW EXECUTE PROCEDURE updated_at_column();
