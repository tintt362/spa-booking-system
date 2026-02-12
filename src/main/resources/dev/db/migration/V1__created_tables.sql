
CREATE TABLE users (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- Thông tin cá nhân
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    date_of_birth DATE,
    gender VARCHAR(10),  -- MALE, FEMALE, OTHER
    avatar_url VARCHAR(500),

    -- Xác thực
    password_hash VARCHAR(255) NOT NULL,
    is_verified BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,

    -- Loyalty
    loyalty_points INTEGER DEFAULT 0,
    membership_tier VARCHAR(20) DEFAULT 'BRONZE',  -- BRONZE, SILVER, GOLD, PLATINUM

    -- Preferences
    preferred_therapist_id BIGINT,
    preferred_service_ids BIGINT[],
    notification_enabled BOOLEAN DEFAULT true,

    -- Timestamps
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    last_login_at TIMESTAMP,
    email_verified_at TIMESTAMP,

    -- Soft delete
    deleted_at TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_preferred_therapist
        FOREIGN KEY (preferred_therapist_id) REFERENCES therapists(id)
);

CREATE TABLE services (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- Thông tin cơ bản
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    short_description VARCHAR(500),

    -- Thông số dịch vụ
    duration_minutes INTEGER NOT NULL,  -- 60, 90, 120
    price DECIMAL(10, 2) NOT NULL,
    discount_price DECIMAL(10, 2),

    -- Media
    image_url VARCHAR(500),
    gallery_urls TEXT[],

    -- Categorization
    category VARCHAR(50),  -- BODY_MASSAGE, FOOT_MASSAGE, FACIAL, COMBO

    -- Display & Status
    is_active BOOLEAN DEFAULT true,
    is_featured BOOLEAN DEFAULT false,
    display_order INTEGER DEFAULT 0,

    -- Booking constraints
    max_bookings_per_day INTEGER DEFAULT 10,
    advance_booking_days INTEGER DEFAULT 30,  -- Đặt trước tối đa bao nhiêu ngày
    min_advance_hours INTEGER DEFAULT 2,      -- Đặt trước tối thiểu bao nhiêu giờ

    -- Loyalty
    loyalty_points_reward INTEGER DEFAULT 0,

    -- Timestamps
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE TABLE booking_slots (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- Foreign Keys
    service_id BIGINT NOT NULL,
    therapist_id BIGINT,  -- NULL = bất kỳ therapist nào

    -- Thời gian
    booking_date DATE NOT NULL,
    booking_time TIME NOT NULL,
    end_time TIME NOT NULL,

    -- Status
    is_booked BOOLEAN DEFAULT false,
    is_blocked BOOLEAN DEFAULT false,  -- Admin block slot này

    -- Booking reference
    booking_id BIGINT,

    -- Admin notes
    block_reason VARCHAR(500),  -- "Therapist nghỉ phép", "Bảo trì thiết bị"

    -- Timestamps
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),

    -- Foreign Keys Constraints
    CONSTRAINT fk_slot_service
        FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE,
    CONSTRAINT fk_slot_therapist
        FOREIGN KEY (therapist_id) REFERENCES therapists(id) ON DELETE CASCADE,
    CONSTRAINT fk_slot_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE SET NULL,

    -- *** CRITICAL: Unique constraint chống trùng lịch ***
    CONSTRAINT unique_therapist_slot UNIQUE (
        therapist_id, booking_date, booking_time
    )
);


CREATE TABLE bookings (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,
    booking_id VARCHAR(50) UNIQUE NOT NULL,  -- BK20250210143022

    -- Customer Information
    customer_name VARCHAR(100) NOT NULL,
    customer_phone VARCHAR(20) NOT NULL,
    customer_email VARCHAR(100),

    -- Foreign Keys
    service_id BIGINT NOT NULL,
    therapist_id BIGINT,
    user_id BIGINT,  -- NULL nếu anonymous

    -- Booking Details
    booking_date DATE NOT NULL,
    booking_time TIME NOT NULL,
    end_time TIME NOT NULL,

    -- Type
    is_anonymous BOOLEAN DEFAULT true,
    booking_source VARCHAR(20) DEFAULT 'WEB',  -- WEB, MOBILE, ADMIN, PHONE

    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    -- PENDING, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW

    -- Pricing
    original_price DECIMAL(10, 2) NOT NULL,
    discount_amount DECIMAL(10, 2) DEFAULT 0,
    final_price DECIMAL(10, 2) NOT NULL,

    -- Payment
    payment_status VARCHAR(20) DEFAULT 'UNPAID',  -- UNPAID, PAID, REFUNDED
    payment_method VARCHAR(20),  -- CASH, CARD, TRANSFER, WALLET

    -- Notes
    customer_note TEXT,
    admin_note TEXT,
    cancellation_reason TEXT,

    -- Reminder
    reminder_sent BOOLEAN DEFAULT false,
    reminder_sent_at TIMESTAMP,

    -- Timestamps
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    confirmed_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,

    -- Foreign Keys Constraints
    CONSTRAINT fk_booking_service
        FOREIGN KEY (service_id) REFERENCES services(id),
    CONSTRAINT fk_booking_therapist
        FOREIGN KEY (therapist_id) REFERENCES therapists(id),
    CONSTRAINT fk_booking_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,

    -- Business Rules
    CONSTRAINT chk_final_price
        CHECK (final_price = original_price - discount_amount),
    CONSTRAINT chk_end_time
        CHECK (end_time > booking_time)
);


CREATE TABLE activity_logs (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- Reference
    booking_id BIGINT NOT NULL,

    -- Action
    action VARCHAR(50) NOT NULL,
    -- BOOKING_CREATED, STATUS_CHANGED, EMAIL_SENT, SMS_SENT,
    -- ADMIN_NOTE_ADDED, PAYMENT_RECEIVED, REMINDER_SENT

    -- Actor
    actor_type VARCHAR(20),  -- CUSTOMER, ADMIN, SYSTEM
    actor_id BIGINT,         -- user_id hoặc admin_id
    actor_name VARCHAR(100), -- Tên người thực hiện

    -- Details
    description TEXT,
    old_value VARCHAR(100),  -- Giá trị cũ (cho status change)
    new_value VARCHAR(100),  -- Giá trị mới

    -- Metadata (flexible JSON)
    metadata JSONB,
    -- Ví dụ: {"ip": "192.168.1.1", "user_agent": "Chrome...", "email_id": "123"}

    -- Timestamp
    created_at TIMESTAMP DEFAULT NOW(),

    -- Foreign Keys
    CONSTRAINT fk_log_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

CREATE TABLE admins (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- Credentials
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,

    -- Personal Info
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    avatar_url VARCHAR(500),

    -- Role & Permissions
    role VARCHAR(20) NOT NULL DEFAULT 'STAFF',
    -- SUPER_ADMIN, ADMIN, STAFF, RECEPTIONIST

    permissions JSONB,
    -- {"bookings": ["view", "edit", "delete"], "services": ["view", "edit"]}

    -- Status
    is_active BOOLEAN DEFAULT true,
    is_super_admin BOOLEAN DEFAULT false,

    -- Security
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,
    must_change_password BOOLEAN DEFAULT false,

    -- Timestamps
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    last_login_at TIMESTAMP,
    password_changed_at TIMESTAMP,

    -- Soft delete
    deleted_at TIMESTAMP
);


CREATE TABLE verification_tokens (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- Reference
    user_id BIGINT NOT NULL,

    -- Token
    token VARCHAR(255) UNIQUE NOT NULL,
    token_type VARCHAR(30) NOT NULL,
    -- EMAIL_VERIFICATION, PASSWORD_RESET, TWO_FACTOR_AUTH

    -- Expiry
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,

    -- Metadata
    ip_address VARCHAR(45),
    user_agent TEXT,

    -- Timestamps
    created_at TIMESTAMP DEFAULT NOW(),

    -- Foreign Keys
    CONSTRAINT fk_token_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    -- Business Rules
    CONSTRAINT chk_not_expired
        CHECK (expires_at > created_at)
);

CREATE TABLE loyalty_transactions (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- Reference
    user_id BIGINT NOT NULL,
    booking_id BIGINT,

    -- Transaction
    points INTEGER NOT NULL,  -- Dương = tích, Âm = tiêu
    transaction_type VARCHAR(20) NOT NULL,
    -- EARN_BOOKING, EARN_REFERRAL, EARN_BIRTHDAY, EARN_PROMOTION,
    -- REDEEM_DISCOUNT, REDEEM_GIFT, EXPIRE, ADJUSTMENT

    balance_before INTEGER NOT NULL,
    balance_after INTEGER NOT NULL,

    -- Details
    description TEXT,
    reference_code VARCHAR(50),  -- Mã voucher, mã giới thiệu...

    -- Expiry (cho điểm có thời hạn)
    expires_at TIMESTAMP,

    -- Admin
    admin_id BIGINT,  -- Nếu admin điều chỉnh điểm

    -- Timestamps
    created_at TIMESTAMP DEFAULT NOW(),

    -- Foreign Keys
    CONSTRAINT fk_loyalty_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_loyalty_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE SET NULL,

    -- Business Rules
    CONSTRAINT chk_balance
        CHECK (balance_after = balance_before + points)
);

CREATE TABLE therapist_schedules (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- Reference
    therapist_id BIGINT NOT NULL,

    -- Date
    schedule_date DATE NOT NULL,

    -- Working hours
    start_time TIME,
    end_time TIME,

    -- Status
    is_working BOOLEAN DEFAULT true,
    is_blocked BOOLEAN DEFAULT false,

    -- Reason
    block_reason VARCHAR(100),
    -- "Nghỉ phép", "Nghỉ ốm", "Đào tạo", "Nghỉ lễ"

    -- Notes
    notes TEXT,

    -- Timestamps
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),

    -- Foreign Keys
    CONSTRAINT fk_schedule_therapist
        FOREIGN KEY (therapist_id) REFERENCES therapists(id) ON DELETE CASCADE,

    -- Unique
    CONSTRAINT unique_therapist_date UNIQUE (therapist_id, schedule_date)
);

CREATE TABLE reviews (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- Reference
    booking_id BIGINT NOT NULL UNIQUE,  -- 1 booking = 1 review
    user_id BIGINT,
    service_id BIGINT NOT NULL,
    therapist_id BIGINT,

    -- Rating (1-5 stars)
    overall_rating INTEGER NOT NULL,
    service_rating INTEGER,
    therapist_rating INTEGER,
    facility_rating INTEGER,

    -- Review
    title VARCHAR(200),
    comment TEXT,

    -- Media
    images TEXT[],

    -- Status
    is_verified BOOLEAN DEFAULT false,  -- Admin đã xác minh
    is_published BOOLEAN DEFAULT false,
    is_featured BOOLEAN DEFAULT false,

    -- Admin
    admin_response TEXT,
    admin_responded_at TIMESTAMP,

    -- Timestamps
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),

    -- Foreign Keys
    CONSTRAINT fk_review_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_review_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_review_service
        FOREIGN KEY (service_id) REFERENCES services(id),
    CONSTRAINT fk_review_therapist
        FOREIGN KEY (therapist_id) REFERENCES therapists(id),

    -- Constraints
    CONSTRAINT chk_ratings
        CHECK (
            overall_rating BETWEEN 1 AND 5 AND
            (service_rating IS NULL OR service_rating BETWEEN 1 AND 5) AND
            (therapist_rating IS NULL OR therapist_rating BETWEEN 1 AND 5) AND
            (facility_rating IS NULL OR facility_rating BETWEEN 1 AND 5)
        )
);

-- Bảng trung gian đơn giản
CREATE TABLE therapist_services (
    id BIGSERIAL PRIMARY KEY,
    therapist_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),

    -- Foreign keys
    CONSTRAINT fk_therapist
        FOREIGN KEY (therapist_id) REFERENCES therapists(id) ON DELETE CASCADE,
    CONSTRAINT fk_service
        FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE,

    -- Unique: 1 therapist không thể có 2 dòng giống nhau với 1 service
    CONSTRAINT unique_therapist_service UNIQUE (therapist_id, service_id)
);