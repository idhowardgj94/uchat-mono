CREATE TABLE IF NOT EXISTS subscriptions (
       id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
       username VARCHAR(255),
       type varchar(255),
       channel_uuid uuid,
       other_user varchar(255),
       unread int,
       last_message_uuid uuid,
       created_at TIMESTAMP default now(),
       updated_at TIMESTAMP default now()
);

CREATE TRIGGER updated_at_subscriptions BEFORE UPDATE ON subscriptions FOR EACH ROW EXECUTE PROCEDURE updated_at_column();
  
