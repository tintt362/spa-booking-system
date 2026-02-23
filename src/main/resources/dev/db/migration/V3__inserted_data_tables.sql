

-- Insert default admin
INSERT INTO admins (username, password_hash, full_name, email, role) VALUES
('admin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYCj.wO0kKS',
 'Super Admin', 'admin@spa.com', 'SUPER_ADMIN');
-- Password: admin123
