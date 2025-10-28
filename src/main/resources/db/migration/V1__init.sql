CREATE TABLE IF NOT EXISTS chat_session (
  id UUID PRIMARY KEY,
  user_id VARCHAR(128) NOT NULL,
  title VARCHAR(255) NOT NULL,
  favorite BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_session_user ON chat_session(user_id);
CREATE INDEX IF NOT EXISTS idx_session_title ON chat_session(title);

CREATE TABLE IF NOT EXISTS chat_message (
  id UUID PRIMARY KEY,
  session_id UUID NOT NULL REFERENCES chat_session(id) ON DELETE CASCADE,
  sender VARCHAR(20) NOT NULL,
  content TEXT NOT NULL,
  context_json TEXT,
  created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_message_session ON chat_message(session_id, created_at);
