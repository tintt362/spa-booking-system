
-- Insert sample data
INSERT INTO services (name, slug, description, duration_minutes, price, category, display_order) VALUES
('Body Massage', 'body-massage', 'Massage toàn thân thư giãn', 90, 500000, 'MASSAGE', 1),
('Foot Massage', 'foot-massage', 'Massage chân giảm mỏi', 60, 300000, 'MASSAGE', 2),
('Facial Treatment', 'facial-treatment', 'Chăm sóc da mặt', 60, 400000, 'FACIAL', 3);

-- Insert sample data
INSERT INTO therapists (full_name, phone, email, employee_code) VALUES
('Nguyễn Thị Mai', '0901111111', 'mai@spa.com', 'TH001'),
('Trần Thị Lan', '0902222222', 'lan@spa.com', 'TH002'),
('Lê Thị Hoa', '0903333333', 'hoa@spa.com', 'TH003');

-- Insert sample data
INSERT INTO therapist_services (therapist_id, service_id, skill_level, years_experience, is_primary_service) VALUES
(1, 1, 'EXPERT', 5, true),    -- Mai: Body Massage (Chuyên môn)
(1, 2, 'INTERMEDIATE', 3, false), -- Mai: Foot Massage
(2, 2, 'EXPERT', 4, true),    -- Lan: Foot Massage (Chuyên môn)
(2, 3, 'INTERMEDIATE', 2, false), -- Lan: Facial
(3, 1, 'INTERMEDIATE', 2, false), -- Hoa: Body Massage
(3, 2, 'INTERMEDIATE', 2, false), -- Hoa: Foot Massage
(3, 3, 'EXPERT', 5, true);    -- Hoa: Facial (Chuyên môn)
