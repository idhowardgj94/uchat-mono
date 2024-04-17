CREATE TABLE IF NOT EXISTS meta (
       id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
       secret bytea,
       version varchar(255),
       created_at TIMESTAMP DEFAULT now(),
       updated_at TIMESTAMP DEFAULT now()
);

CREATE TRIGGER updated_at_meta BEFORE UPDATE ON meta FOR EACH ROW EXECUTE PROCEDURE updated_at_column();
