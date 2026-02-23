-- ============================================================================
-- SPA BOOKING SYSTEM - SAMPLE DATA
-- ============================================================================
-- Môi trường: Development/Testing
-- Mục đích: Test API và demo hệ thống
-- ============================================================================

-- Clear existing data (careful in production!)
--TRUNCATE TABLE activity_logs CASCADE;
--TRUNCATE TABLE loyalty_transactions CASCADE;
--TRUNCATE TABLE verification_tokens CASCADE;
--TRUNCATE TABLE bookings CASCADE;
--TRUNCATE TABLE therapist_services CASCADE;
--TRUNCATE TABLE therapists CASCADE;
--TRUNCATE TABLE services CASCADE;
--TRUNCATE TABLE users CASCADE;
--TRUNCATE TABLE admins CASCADE;
--
---- Reset sequences
--ALTER SEQUENCE users_id_seq RESTART WITH 1;
--ALTER SEQUENCE services_id_seq RESTART WITH 1;
--ALTER SEQUENCE therapists_id_seq RESTART WITH 1;
--ALTER SEQUENCE therapist_services_id_seq RESTART WITH 1;
--ALTER SEQUENCE bookings_id_seq RESTART WITH 1;
--ALTER SEQUENCE activity_logs_id_seq RESTART WITH 1;
--ALTER SEQUENCE loyalty_transactions_id_seq RESTART WITH 1;
--ALTER SEQUENCE verification_tokens_id_seq RESTART WITH 1;
--ALTER SEQUENCE admins_id_seq RESTART WITH 1;

-- ============================================================================
-- 1. ADMINS (Quản trị viên)
-- ============================================================================
-- Password for all: Admin@123 (BCrypt hashed)
INSERT INTO admins (username, password_hash, full_name, email, phone, role, is_active, created_at) VALUES
('superadmin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIq7KZqHiW', 'Super Admin', 'superadmin@spa.com', '0901000001', 'SUPER_ADMIN', true, NOW()),
('admin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIq7KZqHiW', 'Admin User', 'admin@spa.com', '0901000002', 'ADMIN', true, NOW()),
('staff', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIq7KZqHiW', 'Staff User', 'staff@spa.com', '0901000003', 'STAFF', true, NOW()),
('receptionist', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIq7KZqHiW', 'Receptionist', 'receptionist@spa.com', '0901000004', 'RECEPTIONIST', true, NOW());

-- ============================================================================
-- 2. SERVICES (Dịch vụ)
-- ============================================================================
INSERT INTO services (name, slug, description, duration_minutes, price, discount_price, image_url, category, display_order, is_active, created_at) VALUES
-- MASSAGE Services
('Body Massage', 'body-massage',
 'Massage toàn thân thư giãn, giúp giảm stress và căng thẳng. Sử dụng tinh dầu thiên nhiên cao cấp.',
 90, 500000, NULL,
 'https://example.com/images/body-massage.jpg',
 'MASSAGE', 1, true, NOW()),

('Foot Massage', 'foot-massage',
 'Massage chân bấm huyệt, kích thích tuần hoàn máu, giảm mỏi chân sau ngày dài làm việc.',
 60, 300000, 250000,
 'https://example.com/images/foot-massage.jpg',
 'MASSAGE', 2, true, NOW()),

('Hot Stone Massage', 'hot-stone-massage',
 'Massage với đá nóng, giúp thư giãn cơ bắp sâu, tăng tuần hoàn máu và giải độc cơ thể.',
 120, 800000, NULL,
 'https://example.com/images/hot-stone.jpg',
 'MASSAGE', 3, true, NOW()),

('Thai Massage', 'thai-massage',
 'Massage Thái truyền thống, kết hợp duỗi cơ và bấm huyệt, giúp tăng độ dẻo dai.',
 90, 600000, 550000,
 'https://example.com/images/thai-massage.jpg',
 'MASSAGE', 4, true, NOW()),

('Aromatherapy Massage', 'aromatherapy-massage',
 'Massage thư giãn với tinh dầu thơm, giúp cân bằng cảm xúc và giảm căng thẳng.',
 75, 450000, NULL,
 'https://example.com/images/aromatherapy.jpg',
 'MASSAGE', 5, true, NOW()),

-- FACIAL Services
('Deep Cleansing Facial', 'deep-cleansing-facial',
 'Chăm sóc da mặt sâu, làm sạch lỗ chân lông, loại bỏ mụn đầu đen, se khít lỗ chân lông.',
 60, 400000, 350000,
 'https://example.com/images/facial.jpg',
 'FACIAL', 6, true, NOW()),

('Anti-Aging Facial', 'anti-aging-facial',
 'Chăm sóc da chống lão hóa, giảm nếp nhăn, tăng độ săn chắc cho da.',
 90, 700000, NULL,
 'https://example.com/images/anti-aging.jpg',
 'FACIAL', 7, true, NOW()),

('Whitening Facial', 'whitening-facial',
 'Chăm sóc da trắng sáng, đều màu da, giảm thâm nám và tàn nhang.',
 75, 550000, NULL,
 'https://example.com/images/whitening.jpg',
 'FACIAL', 8, true, NOW()),

-- BODY TREATMENT Services
('Body Scrub', 'body-scrub',
 'Tẩy tế bào chết toàn thân, giúp da mịn màng, sáng khỏe và dễ hấp thụ dưỡng chất.',
 60, 450000, NULL,
 'https://example.com/images/body-scrub.jpg',
 'BODY_TREATMENT', 9, true, NOW()),

('Body Wrap', 'body-wrap',
 'Ủ body với bùn khoáng, giúp detox, giảm mỡ và làm săn chắc da.',
 90, 650000, 600000,
 'https://example.com/images/body-wrap.jpg',
 'BODY_TREATMENT', 10, true, NOW()),

-- SPA PACKAGE Services
('Relaxation Package', 'relaxation-package',
 'Combo thư giãn: Body Massage (90 phút) + Foot Massage (30 phút) + Đồ uống miễn phí.',
 120, 700000, 650000,
 'https://example.com/images/relaxation-package.jpg',
 'SPA_PACKAGE', 11, true, NOW()),

('Beauty Package', 'beauty-package',
 'Combo làm đẹp: Deep Cleansing Facial (60 phút) + Body Scrub (60 phút) + Đồ uống.',
 120, 750000, 700000,
 'https://example.com/images/beauty-package.jpg',
 'SPA_PACKAGE', 12, true, NOW()),

('VIP Package', 'vip-package',
 'Trọn gói VIP: Hot Stone Massage (120 phút) + Anti-Aging Facial (90 phút) + Body Wrap (90 phút) + Phòng riêng + Đồ uống cao cấp.',
 300, 2000000, 1800000,
 'https://example.com/images/vip-package.jpg',
 'SPA_PACKAGE', 13, true, NOW()),

-- INACTIVE Service (for testing)
('Old Service', 'old-service',
 'Dịch vụ cũ không còn cung cấp.',
 60, 300000, NULL,
 'https://example.com/images/old.jpg',
 'MASSAGE', 99, false, NOW());

-- ============================================================================
-- 3. THERAPISTS (Nhân viên massage)
-- ============================================================================
INSERT INTO therapists (full_name, phone, email, employee_code, avatar_url, is_active, created_at) VALUES
('Nguyễn Thị Mai', '0902111111', 'mai.nguyen@spa.com', 'TH001', 'https://i.pravatar.cc/150?img=1', true, NOW()),
('Trần Thị Lan', '0902222222', 'lan.tran@spa.com', 'TH002', 'https://i.pravatar.cc/150?img=2', true, NOW()),
('Lê Thị Hoa', '0902333333', 'hoa.le@spa.com', 'TH003', 'https://i.pravatar.cc/150?img=3', true, NOW()),
('Phạm Thị Linh', '0902444444', 'linh.pham@spa.com', 'TH004', 'https://i.pravatar.cc/150?img=4', true, NOW()),
('Hoàng Thị Hương', '0902555555', 'huong.hoang@spa.com', 'TH005', 'https://i.pravatar.cc/150?img=5', true, NOW()),
('Vũ Thị Thanh', '0902666666', 'thanh.vu@spa.com', 'TH006', 'https://i.pravatar.cc/150?img=6', true, NOW()),
('Đặng Thị Ngọc', '0902777777', 'ngoc.dang@spa.com', 'TH007', 'https://i.pravatar.cc/150?img=7', true, NOW()),
('Bùi Thị Thảo', '0902888888', 'thao.bui@spa.com', 'TH008', 'https://i.pravatar.cc/150?img=8', true, NOW()),
-- Inactive therapist (for testing)
('Đỗ Thị Cũ', '0902999999', 'cu.do@spa.com', 'TH009', 'https://i.pravatar.cc/150?img=9', false, NOW());

-- ============================================================================
-- 4. THERAPIST_SERVICES (Ai làm được dịch vụ gì)
-- ============================================================================
-- Therapist 1 - Mai (Expert Body & Thai Massage)
INSERT INTO therapist_services (therapist_id, service_id, skill_level, years_experience, is_primary_service, created_at) VALUES
(1, 1, 'EXPERT', 5, true, NOW()),      -- Body Massage (PRIMARY)
(1, 4, 'EXPERT', 4, false, NOW()),     -- Thai Massage
(1, 5, 'INTERMEDIATE', 3, false, NOW()), -- Aromatherapy
(1, 11, 'EXPERT', 5, false, NOW());    -- Relaxation Package

-- Therapist 2 - Lan (Expert Foot & Hot Stone)
INSERT INTO therapist_services (therapist_id, service_id, skill_level, years_experience, is_primary_service, created_at) VALUES
(2, 2, 'EXPERT', 6, true, NOW()),      -- Foot Massage (PRIMARY)
(2, 3, 'EXPERT', 5, false, NOW()),     -- Hot Stone
(2, 1, 'INTERMEDIATE', 3, false, NOW()); -- Body Massage

-- Therapist 3 - Hoa (Expert Aromatherapy & Thai)
INSERT INTO therapist_services (therapist_id, service_id, skill_level, years_experience, is_primary_service, created_at) VALUES
(3, 5, 'EXPERT', 7, true, NOW()),      -- Aromatherapy (PRIMARY)
(3, 4, 'EXPERT', 6, false, NOW()),     -- Thai Massage
(3, 1, 'INTERMEDIATE', 4, false, NOW()); -- Body Massage

-- Therapist 4 - Linh (Expert Facial)
INSERT INTO therapist_services (therapist_id, service_id, skill_level, years_experience, is_primary_service, created_at) VALUES
(4, 6, 'EXPERT', 5, true, NOW()),      -- Deep Cleansing Facial (PRIMARY)
(4, 7, 'EXPERT', 5, false, NOW()),     -- Anti-Aging Facial
(4, 8, 'INTERMEDIATE', 3, false, NOW()), -- Whitening Facial
(4, 12, 'EXPERT', 5, false, NOW());    -- Beauty Package

-- Therapist 5 - Hương (Expert Body Treatment)
INSERT INTO therapist_services (therapist_id, service_id, skill_level, years_experience, is_primary_service, created_at) VALUES
(5, 9, 'EXPERT', 4, true, NOW()),      -- Body Scrub (PRIMARY)
(5, 10, 'EXPERT', 4, false, NOW()),    -- Body Wrap
(5, 12, 'INTERMEDIATE', 3, false, NOW()); -- Beauty Package

-- Therapist 6 - Thanh (All-rounder for packages)
INSERT INTO therapist_services (therapist_id, service_id, skill_level, years_experience, is_primary_service, created_at) VALUES
(6, 3, 'EXPERT', 6, false, NOW()),     -- Hot Stone
(6, 7, 'EXPERT', 5, false, NOW()),     -- Anti-Aging Facial
(6, 10, 'EXPERT', 5, false, NOW()),    -- Body Wrap
(6, 13, 'EXPERT', 6, true, NOW());     -- VIP Package (PRIMARY)

-- Therapist 7 - Ngọc (Multi-skilled)
INSERT INTO therapist_services (therapist_id, service_id, skill_level, years_experience, is_primary_service, created_at) VALUES
(7, 1, 'INTERMEDIATE', 3, false, NOW()), -- Body Massage
(7, 2, 'INTERMEDIATE', 3, false, NOW()), -- Foot Massage
(7, 6, 'INTERMEDIATE', 2, false, NOW()), -- Facial
(7, 9, 'INTERMEDIATE', 2, true, NOW());  -- Body Scrub (PRIMARY)

-- Therapist 8 - Thảo (Beginner, for training)
INSERT INTO therapist_services (therapist_id, service_id, skill_level, years_experience, is_primary_service, created_at) VALUES
(8, 2, 'BEGINNER', 1, true, NOW()),    -- Foot Massage (PRIMARY)
(8, 9, 'BEGINNER', 1, false, NOW());   -- Body Scrub

-- ============================================================================
-- 5. USERS (Khách hàng)
-- ============================================================================
-- Password for all: User@123 (BCrypt hashed)
INSERT INTO users (full_name, email, phone, password_hash, date_of_birth, gender, loyalty_points, membership_tier, is_verified, is_active, email_verified_at, last_login_at, created_at) VALUES
-- PLATINUM tier
('Nguyễn Văn Anh', 'anh.nguyen@example.com', '0903111111',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIq7KZqHiW',
 '1985-05-15', 'MALE', 5500, 'PLATINUM', true, true, NOW() - INTERVAL '30 days', NOW() - INTERVAL '1 day', NOW() - INTERVAL '365 days'),

-- GOLD tier
('Trần Thị Bình', 'binh.tran@example.com', '0903222222',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIq7KZqHiW',
 '1990-08-20', 'FEMALE', 3200, 'GOLD', true, true, NOW() - INTERVAL '60 days', NOW() - INTERVAL '2 days', NOW() - INTERVAL '200 days'),

('Lê Văn Cường', 'cuong.le@example.com', '0903333333',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIq7KZqHiW',
 '1988-03-10', 'MALE', 2800, 'GOLD', true, true, NOW() - INTERVAL '45 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '180 days'),

-- SILVER tier
('Phạm Thị Dung', 'dung.pham@example.com', '0903444444',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIq7KZqHiW',
 '1992-11-25', 'FEMALE', 1200, 'SILVER', true, true, NOW() - INTERVAL '20 days', NOW() - INTERVAL '3 days', NOW() - INTERVAL '120 days'),

('Hoàng Văn Em', 'em.hoang@example.com', '0903555555',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIq7KZqHiW',
 '1995-07-18', 'MALE', 800, 'SILVER', true, true, NOW() - INTERVAL '15 days', NOW() - INTERVAL '1 hour', NOW() - INTERVAL '90 days'),

-- BRONZE tier
('Vũ Thị Phương', 'phuong.vu@example.com', '0903666666',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIq7KZqHiW',
 '1998-01-30', 'FEMALE', 250, 'BRONZE', true, true, NOW() - INTERVAL '10 days', NOW() - INTERVAL '1 day', NOW() - INTERVAL '30 days'),

('Đặng Văn Giang', 'giang.dang@example.com', '0903777777',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIq7KZqHiW',
 '2000-09-05', 'MALE', 150, 'BRONZE', true, true, NOW() - INTERVAL '5 days', NOW(), NOW() - INTERVAL '15 days'),

('Bùi Thị Hằng', 'hang.bui@example.com', '0903888888',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIq7KZqHiW',
 '1993-12-12', 'FEMALE', 80, 'BRONZE', true, true, NOW() - INTERVAL '7 days', NOW() - INTERVAL '6 hours', NOW() - INTERVAL '20 days'),

-- New user (just registered, no bookings yet)
('Cao Văn Ích', 'ich.cao@example.com', '0903999999',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIq7KZqHiW',
 '1997-04-22', 'MALE', 0, 'BRONZE', true, true, NOW(), NOW(), NOW()),

-- Unverified user (for testing email verification)
('Đỗ Thị Kim', 'kim.do@example.com', '0903000000',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIq7KZqHiW',
 '1996-06-14', 'FEMALE', 0, 'BRONZE', false, true, NULL, NULL, NOW());

-- ============================================================================
-- 6. BOOKINGS (Đặt lịch)
-- ============================================================================

-- ========== COMPLETED BOOKINGS (Past - for history) ==========
-- User 1 (PLATINUM) - Many bookings
INSERT INTO bookings (booking_id, customer_name, customer_phone, customer_email, user_id, service_id, therapist_id, booking_date, booking_time, end_time, is_anonymous, booking_source, status, original_price, discount_amount, final_price, payment_status, payment_method, customer_note, created_at, confirmed_at, completed_at) VALUES
('BK20250115100000', 'Nguyễn Văn Anh', '0903111111', 'anh.nguyen@example.com', 1, 1, 1,
 '2025-01-15', '10:00', '11:30', false, 'WEB', 'COMPLETED',
 500000, 50000, 450000, 'PAID', 'CARD', 'Muốn phòng yên tĩnh',
 '2025-01-14 09:00:00', '2025-01-14 10:00:00', '2025-01-15 11:30:00'),

('BK20250118140000', 'Nguyễn Văn Anh', '0903111111', 'anh.nguyen@example.com', 1, 3, 2,
 '2025-01-18', '14:00', '16:00', false, 'WEB', 'COMPLETED',
 800000, 100000, 700000, 'PAID', 'TRANSFER', NULL,
 '2025-01-17 08:00:00', '2025-01-17 09:00:00', '2025-01-18 16:00:00'),

('BK20250122090000', 'Nguyễn Văn Anh', '0903111111', 'anh.nguyen@example.com', 1, 13, 6,
 '2025-01-22', '09:00', '14:00', false, 'MOBILE', 'COMPLETED',
 2000000, 200000, 1800000, 'PAID', 'CARD', 'Đặt phòng VIP',
 '2025-01-20 15:00:00', '2025-01-20 16:00:00', '2025-01-22 14:00:00');

-- User 2 (GOLD) - Several bookings
INSERT INTO bookings (booking_id, customer_name, customer_phone, customer_email, user_id, service_id, therapist_id, booking_date, booking_time, end_time, is_anonymous, booking_source, status, original_price, discount_amount, final_price, payment_status, payment_method, created_at, confirmed_at, completed_at) VALUES
('BK20250116110000', 'Trần Thị Bình', '0903222222', 'binh.tran@example.com', 2, 6, 4,
 '2025-01-16', '11:00', '12:00', false, 'WEB', 'COMPLETED',
 400000, 30000, 370000, 'PAID', 'CASH', NULL,
 '2025-01-15 10:00:00', '2025-01-15 11:00:00', '2025-01-16 12:00:00'),

('BK20250120150000', 'Trần Thị Bình', '0903222222', 'binh.tran@example.com', 2, 10, 5,
 '2025-01-20', '15:00', '16:30', false, 'WEB', 'COMPLETED',
 650000, 50000, 600000, 'PAID', 'CARD', 'Dị ứng hương nhu',
 '2025-01-19 09:00:00', '2025-01-19 10:00:00', '2025-01-20 16:30:00');

-- User 3 (GOLD)
INSERT INTO bookings (booking_id, customer_name, customer_phone, customer_email, user_id, service_id, therapist_id, booking_date, booking_time, end_time, is_anonymous, booking_source, status, original_price, discount_amount, final_price, payment_status, payment_method, created_at, confirmed_at, completed_at) VALUES
('BK20250117140000', 'Lê Văn Cường', '0903333333', 'cuong.le@example.com', 3, 4, 3,
 '2025-01-17', '14:00', '15:30', false, 'MOBILE', 'COMPLETED',
 600000, 50000, 550000, 'PAID', 'WALLET', NULL,
 '2025-01-16 08:00:00', '2025-01-16 09:00:00', '2025-01-17 15:30:00');

-- User 4 (SILVER)
INSERT INTO bookings (booking_id, customer_name, customer_phone, customer_email, user_id, service_id, therapist_id, booking_date, booking_time, end_time, is_anonymous, booking_source, status, original_price, discount_amount, final_price, payment_status, payment_method, created_at, confirmed_at, completed_at) VALUES
('BK20250119100000', 'Phạm Thị Dung', '0903444444', 'dung.pham@example.com', 4, 2, 2,
 '2025-01-19', '10:00', '11:00', false, 'WEB', 'COMPLETED',
 300000, 20000, 280000, 'PAID', 'CASH', NULL,
 '2025-01-18 15:00:00', '2025-01-18 16:00:00', '2025-01-19 11:00:00');

-- User 5 (SILVER)
INSERT INTO bookings (booking_id, customer_name, customer_phone, customer_email, user_id, service_id, therapist_id, booking_date, booking_time, end_time, is_anonymous, booking_source, status, original_price, discount_amount, final_price, payment_status, payment_method, created_at, confirmed_at, completed_at) VALUES
('BK20250121130000', 'Hoàng Văn Em', '0903555555', 'em.hoang@example.com', 5, 1, 1,
 '2025-01-21', '13:00', '14:30', false, 'WEB', 'COMPLETED',
 500000, 30000, 470000, 'PAID', 'CARD', 'Lần đầu đến',
 '2025-01-20 10:00:00', '2025-01-20 11:00:00', '2025-01-21 14:30:00');

-- Anonymous bookings (COMPLETED)
INSERT INTO bookings (booking_id, customer_name, customer_phone, customer_email, user_id, service_id, therapist_id, booking_date, booking_time, end_time, is_anonymous, booking_source, status, original_price, discount_amount, final_price, payment_status, payment_method, created_at, confirmed_at, completed_at) VALUES
('BK20250118100000', 'Nguyễn Thị Lan', '0904111111', 'lan.nguyen.guest@gmail.com', NULL, 2, 2,
 '2025-01-18', '10:00', '11:00', true, 'WEB', 'COMPLETED',
 300000, 0, 300000, 'PAID', 'CASH', NULL,
 '2025-01-17 14:00:00', '2025-01-17 15:00:00', '2025-01-18 11:00:00'),

('BK20250120100000', 'Trần Văn Minh', '0904222222', 'minh.tran.guest@gmail.com', NULL, 1, 3,
 '2025-01-20', '10:00', '11:30', true, 'PHONE', 'COMPLETED',
 500000, 0, 500000, 'PAID', 'CASH', 'Khách walk-in',
 '2025-01-20 09:30:00', '2025-01-20 09:30:00', '2025-01-20 11:30:00');

-- ========== CONFIRMED BOOKINGS (Upcoming) ==========
INSERT INTO bookings (booking_id, customer_name, customer_phone, customer_email, user_id, service_id, therapist_id, booking_date, booking_time, end_time, is_anonymous, booking_source, status, original_price, discount_amount, final_price, payment_status, payment_method, customer_note, reminder_sent, created_at, confirmed_at) VALUES
-- Tomorrow bookings
('BK20250222090000', 'Nguyễn Văn Anh', '0903111111', 'anh.nguyen@example.com', 1, 1, 1,
 CURRENT_DATE + INTERVAL '1 day', '09:00', '10:30', false, 'WEB', 'CONFIRMED',
 500000, 50000, 450000, 'UNPAID', NULL, 'Prefer male therapist', true,
 NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days'),

('BK20250222140000', 'Trần Thị Bình', '0903222222', 'binh.tran@example.com', 2, 7, 4,
 CURRENT_DATE + INTERVAL '1 day', '14:00', '15:30', false, 'MOBILE', 'CONFIRMED',
 700000, 50000, 650000, 'UNPAID', NULL, NULL, true,
 NOW() - INTERVAL '4 days', NOW() - INTERVAL '3 days'),

-- Day after tomorrow
('BK20250223100000', 'Lê Văn Cường', '0903333333', 'cuong.le@example.com', 3, 11, 1,
 CURRENT_DATE + INTERVAL '2 days', '10:00', '12:00', false, 'WEB', 'CONFIRMED',
 700000, 70000, 630000, 'UNPAID', NULL, NULL, false,
 NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day'),

('BK20250223160000', 'Phạm Thị Dung', '0903444444', 'dung.pham@example.com', 4, 12, 4,
 CURRENT_DATE + INTERVAL '2 days', '16:00', '18:00', false, 'WEB', 'CONFIRMED',
 750000, 50000, 700000, 'UNPAID', NULL, 'First time spa package', false,
 NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day'),

-- Next week
('BK20250228110000', 'Hoàng Văn Em', '0903555555', 'em.hoang@example.com', 5, 3, 2,
 CURRENT_DATE + INTERVAL '6 days', '11:00', '13:00', false, 'WEB', 'CONFIRMED',
 800000, 80000, 720000, 'UNPAID', NULL, NULL, false,
 NOW() - INTERVAL '1 day', NOW()),

-- Anonymous confirmed booking
('BK20250224140000', 'Vũ Thị Oanh', '0904333333', 'oanh.vu.guest@gmail.com', NULL, 2, 8,
 CURRENT_DATE + INTERVAL '3 days', '14:00', '15:00', true, 'PHONE', 'CONFIRMED',
 300000, 0, 300000, 'UNPAID', NULL, 'Call to confirm', false,
 NOW() - INTERVAL '12 hours', NOW() - INTERVAL '11 hours');

-- ========== PENDING BOOKINGS (Awaiting confirmation) ==========
INSERT INTO bookings (booking_id, customer_name, customer_phone, customer_email, user_id, service_id, therapist_id, booking_date, booking_time, end_time, is_anonymous, booking_source, status, original_price, discount_amount, final_price, payment_status, customer_note, created_at) VALUES
-- Today's pending
('BK20250221160000', 'Vũ Thị Phương', '0903666666', 'phuong.vu@example.com', 6, 5, 3,
 CURRENT_DATE, '16:00', '17:15', false, 'WEB', 'PENDING',
 450000, 20000, 430000, 'UNPAID', 'Allergic to lavender',
 NOW() - INTERVAL '3 hours'),

-- Tomorrow's pending
('BK20250222160000', 'Đặng Văn Giang', '0903777777', 'giang.dang@example.com', 7, 9, 7,
 CURRENT_DATE + INTERVAL '1 day', '16:00', '17:00', false, 'MOBILE', 'PENDING',
 450000, 15000, 435000, 'UNPAID', NULL,
 NOW() - INTERVAL '5 hours'),

-- Next week pending
('BK20250227150000', 'Bùi Thị Hằng', '0903888888', 'hang.bui@example.com', 8, 6, 4,
 CURRENT_DATE + INTERVAL '5 days', '15:00', '16:00', false, 'WEB', 'PENDING',
 400000, 10000, 390000, 'UNPAID', 'First facial',
 NOW() - INTERVAL '1 hour'),

-- Anonymous pending
('BK20250225110000', 'Cao Thị Phượng', '0904444444', 'phuong.cao.guest@gmail.com', NULL, 1, 1,
 CURRENT_DATE + INTERVAL '4 days', '11:00', '12:30', true, 'PHONE', 'PENDING',
 500000, 0, 500000, 'UNPAID', 'Walk-in customer',
 NOW() - INTERVAL '30 minutes');

-- ========== CANCELLED BOOKINGS ==========
INSERT INTO bookings (booking_id, customer_name, customer_phone, customer_email, user_id, service_id, therapist_id, booking_date, booking_time, end_time, is_anonymous, booking_source, status, original_price, discount_amount, final_price, payment_status, customer_note, cancellation_reason, created_at, cancelled_at) VALUES
('BK20250119150000', 'Nguyễn Văn Anh', '0903111111', 'anh.nguyen@example.com', 1, 4, 3,
 '2025-01-19', '15:00', '16:30', false, 'WEB', 'CANCELLED',
 600000, 60000, 540000, 'UNPAID', NULL, 'Lịch trình thay đổi đột xuất',
 '2025-01-17 10:00:00', '2025-01-18 09:00:00'),

('BK20250120180000', 'Trần Thị Bình', '0903222222', 'binh.tran@example.com', 2, 8, 4,
 '2025-01-20', '18:00', '19:15', false, 'WEB', 'CANCELLED',
 550000, 50000, 500000, 'UNPAID', NULL, 'Bận công việc',
 '2025-01-18 14:00:00', '2025-01-19 10:00:00'),

-- Anonymous cancelled
('BK20250121100000', 'Đỗ Văn Quân', '0904555555', 'quan.do.guest@gmail.com', NULL, 2, 2,
 '2025-01-21', '10:00', '11:00', true, 'PHONE', 'CANCELLED',
 300000, 0, 300000, 'UNPAID', NULL, 'Customer cancelled via phone',
 '2025-01-19 15:00:00', '2025-01-20 09:00:00');

-- ========== NO SHOW ==========
INSERT INTO bookings (booking_id, customer_name, customer_phone, customer_email, user_id, service_id, therapist_id, booking_date, booking_time, end_time, is_anonymous, booking_source, status, original_price, discount_amount, final_price, payment_status, admin_note, created_at, confirmed_at) VALUES
('BK20250118150000', 'Lê Văn Cường', '0903333333', 'cuong.le@example.com', 3, 2, 2,
 '2025-01-18', '15:00', '16:00', false, 'WEB', 'NO_SHOW',
 300000, 30000, 270000, 'UNPAID', 'Customer did not show up. Phone unreachable.',
 '2025-01-16 11:00:00', '2025-01-16 12:00:00');

-- ============================================================================
-- 7. ACTIVITY LOGS (Lịch sử hoạt động)
-- ============================================================================
-- Sample activity logs for first booking
INSERT INTO activity_logs (booking_id, action, actor_type, actor_id, actor_name, description, old_value, new_value, created_at) VALUES
-- Booking created
(1, 'BOOKING_CREATED', 'CUSTOMER', 1, 'Nguyễn Văn Anh', 'Khách hàng tạo booking', NULL, NULL, '2025-01-14 09:00:00'),

-- Admin confirmed
(1, 'STATUS_CHANGED', 'ADMIN', 2, 'Admin User', 'Thay đổi trạng thái: PENDING → CONFIRMED', 'PENDING', 'CONFIRMED', '2025-01-14 10:00:00'),

-- Reminder sent
(1, 'REMINDER_SENT', 'SYSTEM', NULL, 'System', 'Gửi email nhắc nhở', NULL, NULL, '2025-01-14 10:00:00'),

-- Completed
(1, 'STATUS_CHANGED', 'ADMIN', 3, 'Staff User', 'Thay đổi trạng thái: CONFIRMED → COMPLETED', 'CONFIRMED', 'COMPLETED', '2025-01-15 11:30:00'),

-- Points awarded
(1, 'POINTS_AWARDED', 'SYSTEM', NULL, 'System', 'Tích điểm từ booking hoàn thành: +45 điểm', NULL, NULL, '2025-01-15 11:31:00');

-- Sample activity logs for cancelled booking
INSERT INTO activity_logs (booking_id, action, actor_type, actor_id, actor_name, description, old_value, new_value, created_at) VALUES
(21, 'BOOKING_CREATED', 'CUSTOMER', 1, 'Nguyễn Văn Anh', 'Khách hàng tạo booking', NULL, NULL, '2025-01-17 10:00:00'),
(21, 'STATUS_CHANGED', 'CUSTOMER', 1, 'Nguyễn Văn Anh', 'Thay đổi trạng thái: PENDING → CANCELLED. Lý do: Lịch trình thay đổi đột xuất', 'PENDING', 'CANCELLED', '2025-01-18 09:00:00'),
(21, 'POINTS_REFUNDED', 'SYSTEM', NULL, 'System', 'Hoàn điểm do hủy booking: +60 điểm', NULL, NULL, '2025-01-18 09:00:01');

-- ============================================================================
-- 8. LOYALTY TRANSACTIONS (Giao dịch điểm)
-- ============================================================================

-- User 1 (PLATINUM - 5500 points) - Transaction history
INSERT INTO loyalty_transactions (user_id, booking_id, points, transaction_type, balance_before, balance_after, description, reference_code, created_at) VALUES
-- Earn from bookings
(1, 1, 45, 'EARN_BOOKING', 5000, 5045, 'Tích điểm từ booking BK20250115100000', 'BK20250115100000', '2025-01-15 11:31:00'),
(1, 2, 70, 'EARN_BOOKING', 5045, 5115, 'Tích điểm từ booking BK20250118140000', 'BK20250118140000', '2025-01-18 16:00:01'),
(1, 3, 180, 'EARN_BOOKING', 5115, 5295, 'Tích điểm từ booking BK20250122090000', 'BK20250122090000', '2025-01-22 14:00:01'),

-- Redeem points for discount
(1, 1, -50, 'REDEEM_DISCOUNT', 5000, 4950, 'Sử dụng điểm cho booking BK20250115100000', 'BK20250115100000', '2025-01-14 09:00:00'),
(1, 2, -100, 'REDEEM_DISCOUNT', 5045, 4945, 'Sử dụng điểm cho booking BK20250118140000', 'BK20250118140000', '2025-01-17 08:00:00'),
(1, 3, -200, 'REDEEM_DISCOUNT', 5115, 4915, 'Sử dụng điểm cho booking BK20250122090000', 'BK20250122090000', '2025-01-20 15:00:00'),

-- Refund from cancelled booking
(1, 21, 60, 'REFUND_CANCELLATION', 5295, 5355, 'Hoàn điểm do hủy booking BK20250119150000', 'BK20250119150000', '2025-01-18 09:00:01'),

-- Birthday bonus
(1, NULL, 100, 'BIRTHDAY_BONUS', 5355, 5455, 'Điểm thưởng sinh nhật', 'BIRTHDAY_2025', '2025-05-15 00:00:00'),

-- Referral bonus
(1, NULL, 100, 'REFERRAL_BONUS', 5455, 5555, 'Giới thiệu bạn bè: Trần Thị Bình', 'REF_USER2', '2025-01-10 10:00:00'),

-- Admin adjustment (customer service compensation)
(1, NULL, -55, 'ADJUSTMENT', 5555, 5500, 'Điều chỉnh: Compensation for service delay', 'ADJ_20250123_001', '2025-01-23 14:00:00');

-- User 2 (GOLD - 3200 points)
INSERT INTO loyalty_transactions (user_id, booking_id, points, transaction_type, balance_before, balance_after, description, reference_code, created_at) VALUES
(2, 4, 37, 'EARN_BOOKING', 3000, 3037, 'Tích điểm từ booking BK20250116110000', 'BK20250116110000', '2025-01-16 12:00:01'),
(2, 5, 60, 'EARN_BOOKING', 3037, 3097, 'Tích điểm từ booking BK20250120150000', 'BK20250120150000', '2025-01-20 16:30:01'),
(2, 4, -30, 'REDEEM_DISCOUNT', 3000, 2970, 'Sử dụng điểm cho booking', 'BK20250116110000', '2025-01-15 10:00:00'),
(2, 5, -50, 'REDEEM_DISCOUNT', 3037, 2987, 'Sử dụng điểm cho booking', 'BK20250120150000', '2025-01-19 09:00:00'),
(2, 22, 50, 'REFUND_CANCELLATION', 3097, 3147, 'Hoàn điểm do hủy booking', 'BK20250120180000', '2025-01-19 10:00:01'),
(2, NULL, 100, 'REFERRAL_BONUS', 3147, 3247, 'Giới thiệu bạn bè: Lê Văn Cường', 'REF_USER3', '2025-01-12 11:00:00');

-- User 3 (GOLD - 2800 points)
INSERT INTO loyalty_transactions (user_id, booking_id, points, transaction_type, balance_before, balance_after, description, reference_code, created_at) VALUES
(3, 6, 55, 'EARN_BOOKING', 2700, 2755, 'Tích điểm từ booking', 'BK20250117140000', '2025-01-17 15:30:01'),
(3, 6, -50, 'REDEEM_DISCOUNT', 2700, 2650, 'Sử dụng điểm cho booking', 'BK20250117140000', '2025-01-16 08:00:00'),
(3, NULL, 95, 'TIER_UPGRADE_BONUS', 2755, 2850, 'Thưởng nâng hạng GOLD', 'TIER_GOLD', '2025-01-17 15:31:00');

-- User 4 (SILVER - 1200 points)
INSERT INTO loyalty_transactions (user_id, booking_id, points, transaction_type, balance_before, balance_after, description, reference_code, created_at) VALUES
(4, 7, 28, 'EARN_BOOKING', 1150, 1178, 'Tích điểm từ booking', 'BK20250119100000', '2025-01-19 11:00:01'),
(4, 7, -20, 'REDEEM_DISCOUNT', 1150, 1130, 'Sử dụng điểm cho booking', 'BK20250119100000', '2025-01-18 15:00:00'),
(4, NULL, 50, 'TIER_UPGRADE_BONUS', 1178, 1228, 'Thưởng nâng hạng SILVER', 'TIER_SILVER', '2025-01-19 11:01:00');

-- User 5 (SILVER - 800 points)
INSERT INTO loyalty_transactions (user_id, booking_id, points, transaction_type, balance_before, balance_after, description, reference_code, created_at) VALUES
(5, 8, 47, 'EARN_BOOKING', 730, 777, 'Tích điểm từ booking', 'BK20250121130000', '2025-01-21 14:30:01'),
(5, 8, -30, 'REDEEM_DISCOUNT', 730, 700, 'Sử dụng điểm cho booking', 'BK20250121130000', '2025-01-20 10:00:00'),
(5, NULL, 50, 'WELCOME_BONUS', 0, 50, 'Điểm thưởng đăng ký', 'WELCOME', '2024-11-02 10:00:00');

-- User 6 (BRONZE - 250 points)
INSERT INTO loyalty_transactions (user_id, booking_id, points, transaction_type, balance_before, balance_after, description, reference_code, created_at) VALUES
(6, NULL, 50, 'WELCOME_BONUS', 0, 50, 'Điểm thưởng đăng ký', 'WELCOME', '2025-01-12 14:00:00'),
(6, 16, -20, 'REDEEM_DISCOUNT', 200, 180, 'Sử dụng điểm cho booking', 'BK20250221160000', NOW() - INTERVAL '3 hours');

-- User 7 (BRONZE - 150 points)
INSERT INTO loyalty_transactions (user_id, booking_id, points, transaction_type, balance_before, balance_after, description, reference_code, created_at) VALUES
(7, NULL, 50, 'WELCOME_BONUS', 0, 50, 'Điểm thưởng đăng ký', 'WELCOME', '2025-02-06 16:00:00'),
(7, 17, -15, 'REDEEM_DISCOUNT', 100, 85, 'Sử dụng điểm cho booking', 'BK20250222160000', NOW() - INTERVAL '5 hours');

-- ============================================================================
-- 9. VERIFICATION TOKENS (Token xác thực)
-- ============================================================================
-- Used tokens (already verified)
INSERT INTO verification_tokens (user_id, token, token_type, expires_at, used_at, ip_address, user_agent, created_at) VALUES
(1, 'a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d', 'EMAIL_VERIFICATION', NOW() + INTERVAL '24 hours', NOW() - INTERVAL '30 days', '192.168.1.100', 'Mozilla/5.0...', NOW() - INTERVAL '30 days'),
(2, 'b2c3d4e5-f6a7-4b8c-9d0e-1f2a3b4c5d6e', 'EMAIL_VERIFICATION', NOW() + INTERVAL '24 hours', NOW() - INTERVAL '60 days', '192.168.1.101', 'Mozilla/5.0...', NOW() - INTERVAL '60 days'),
(3, 'c3d4e5f6-a7b8-4c9d-0e1f-2a3b4c5d6e7f', 'EMAIL_VERIFICATION', NOW() + INTERVAL '24 hours', NOW() - INTERVAL '45 days', '192.168.1.102', 'Mozilla/5.0...', NOW() - INTERVAL '45 days');

-- Active token for unverified user
INSERT INTO verification_tokens (user_id, token, token_type, expires_at, used_at, ip_address, user_agent, created_at) VALUES
(10, 'z9y8x7w6-v5u4-4t3s-2r1q-0p9o8n7m6l5k', 'EMAIL_VERIFICATION', NOW() + INTERVAL '24 hours', NULL, '192.168.1.110', 'Mozilla/5.0...', NOW());

-- Expired token (for testing)
INSERT INTO verification_tokens (user_id, token, token_type, expires_at, used_at, ip_address, user_agent, created_at) VALUES
(10, 'expired-token-123456789', 'EMAIL_VERIFICATION', NOW() - INTERVAL '1 day', NULL, '192.168.1.110', 'Mozilla/5.0...', NOW() - INTERVAL '2 days');

-- Password reset tokens
INSERT INTO verification_tokens (user_id, token, token_type, expires_at, used_at, ip_address, user_agent, created_at) VALUES
-- Used password reset
(1, 'reset-used-token-abc123', 'PASSWORD_RESET', NOW() + INTERVAL '1 hour', NOW() - INTERVAL '5 days', '192.168.1.100', 'Mozilla/5.0...', NOW() - INTERVAL '5 days'),
-- Active password reset (for testing)
(2, 'reset-active-token-xyz789', 'PASSWORD_RESET', NOW() + INTERVAL '1 hour', NULL, '192.168.1.101', 'Mozilla/5.0...', NOW() - INTERVAL '10 minutes');

