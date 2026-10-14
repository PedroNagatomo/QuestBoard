ALTER TABLE users
    ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';

CREATE INDEX idx_users_role ON users(role);

-- Admin como ADMIN
UPDATE users SET role = 'ADMIN' WHERE username = 'admin';