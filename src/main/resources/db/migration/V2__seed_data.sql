-- V2__seed_data.sql
-- Default test credentials:
-- 1. admin@example.com / admin123 (Role: ADMIN)
-- 2. user@example.com  / user123  (Role: USER)

INSERT INTO users (name, email, password, role, created_at, updated_at)
VALUES 
('Admin Manager', 'admin@example.com', '$2a$10$GsN8Th92Xa46bs2zkWAwsO63xDNQHEc5qpQ1c/EWSslnCQA45uQNW', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('John Doe', 'user@example.com', '$2a$10$TalLbDrICLB90k/dc1CSrOJHLvsXBeChBgNLX.eM0.Ks7CXUmhIsW', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

INSERT INTO tasks (title, description, status, due_date, assigned_to_id, created_at, updated_at)
VALUES
('Setup Spring Boot Architecture', 'Convert Express architecture to Spring Boot 3 with JPA and Redis', 'DONE', CURRENT_TIMESTAMP + INTERVAL '3 days', (SELECT id FROM users WHERE email = 'admin@example.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Configure Swagger UI Documentation', 'Verify interactive OpenAPI endpoints and JWT authentication support', 'IN_PROGRESS', CURRENT_TIMESTAMP + INTERVAL '5 days', (SELECT id FROM users WHERE email = 'user@example.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Perform End-to-End Testing', 'Execute comprehensive tests for authentication, task CRUD, and Redis caching', 'PENDING', CURRENT_TIMESTAMP + INTERVAL '7 days', (SELECT id FROM users WHERE email = 'user@example.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;
