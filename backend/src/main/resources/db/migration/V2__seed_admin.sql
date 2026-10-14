-- Admin/demo user: login `admin` / senha `admin123`
-- Hash BCrypt gerado com força 10
INSERT INTO users (username, email, password_hash, display_name, player_class, level, xp, total_xp, coins)
VALUES (
           'admin',
           'admin@questboard.dev',
           '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
           'Admin QuestBoard',
           'PM',
           1, 0, 0, 100
       );