CREATE TABLE IF NOT EXISTS channels (
       uuid uuid DEFAULT gen_random_uuid() PRIMARY KEY,
       team_uuid uuid,
       name VARCHAR(255),
       created_at TIMESTAMP DEFAULT now(),
       updated_at TIMESTAMP DEFAULT now()
);

CREATE TRIGGER updated_at_groups BEFORE UPDATE ON groups FOR EACH ROW EXECUTE PROCEDURE updated_at_column();
