-- Mais 2 usuários de demonstração (senha 'admin123' bcrypt)
INSERT INTO users (username, email, password_hash, display_name, player_class, level, xp, total_xp, coins)
VALUES
    ('alice', 'alice@questboard.dev',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Alice Dev', 'DEV', 1, 0, 0, 50),
    ('bob',   'bob@questboard.dev',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Bob Designer', 'DESIGNER', 1, 0, 0, 50)
    ON CONFLICT (username) DO NOTHING;