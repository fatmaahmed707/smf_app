-- V2__seed_data.sql

-- Insert roles
INSERT INTO roles (role_name) VALUES
('ADMIN'),
('USER')
ON CONFLICT (role_name) DO NOTHING;

-- Insert users
INSERT INTO users (username, email, password) VALUES
('admin', 'admin@smf.com', '$2a$10$YOUR_ADMIN_HASH'),
('user',  'user@smf.com',  '$2a$10$YOUR_USER_HASH')
ON CONFLICT (email) DO NOTHING;

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON (u.email = 'admin@smf.com' AND r.role_name = 'ADMIN')
             OR (u.email = 'user@smf.com'  AND r.role_name = 'USER')
ON CONFLICT (user_id, role_id) DO NOTHING;
