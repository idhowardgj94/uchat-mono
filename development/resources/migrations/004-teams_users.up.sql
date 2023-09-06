
CREATE TABLE IF NOT EXISTS teams_users (
       id bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
       username VARCHAR(255),
       team_uuid uuid,
       created_at TIMESTAMP SET DEFAULT now(),
       updated_at TIMESTAMP SET DEFAULT now()
);

CREATE TRIGGER update_at_teams_users BEFORE UPDATE ON teams_users FOR EACH ROW EXECUTE PROCEDURE updated_at_column();
  
