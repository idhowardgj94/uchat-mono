CREATE TABLE IF NOT EXISTS messages (
       uuid uuid DEFAULT gen_random_uuid() PRIMARY KEY,
       channel_uuid uuid,
       username VARCHAR(255),
       msg text,
       created_at TIMESTAMP DEFAULT now(),
       updated_at TIMESTAMP DEFAULT now()
);

CREATE TRIGGER updated_at_messages BEFORE UPDATE ON messages FOR EACH ROW EXECUTE PROCEDURE updated_at_column();
