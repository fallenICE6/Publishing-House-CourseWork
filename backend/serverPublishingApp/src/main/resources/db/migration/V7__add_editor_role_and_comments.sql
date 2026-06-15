ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('author', 'admin', 'reviewer', 'editor'));

CREATE TABLE IF NOT EXISTS order_comments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    comment TEXT NOT NULL,
    is_system BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_order_comments_order_id ON order_comments(order_id);
CREATE INDEX IF NOT EXISTS idx_order_comments_created_at ON order_comments(created_at);

ALTER TABLE orders ADD COLUMN IF NOT EXISTS ready_for_review BOOLEAN DEFAULT FALSE;