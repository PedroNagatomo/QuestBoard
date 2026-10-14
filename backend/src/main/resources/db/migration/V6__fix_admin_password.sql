-- Corrige o hash BCrypt do admin (senha: admin123)
-- Hash gerado com BCryptPasswordEncoder(10) em 2026-10-13
UPDATE users
SET password_hash = '$2a$10$z0GXej5ui.a7mffvKFiec.61jSz9fAtxd4J2xvEGYwBq53DaQ3Mpm'
WHERE username = 'admin';