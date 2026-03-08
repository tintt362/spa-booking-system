# 🌸 Spa Booking System (Spring Boot)

A complete booking management system for spa/salon businesses with **real-time availability**, **double-booking prevention**, **loyalty program**, **admin dashboard**, and **email notifications**.

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.x-brightgreen.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

---

## 📌 Overview

**Spa Booking System** is a backend project built with **Spring Boot + PostgreSQL**.

It supports:
- Booking for **registered users** and **walk-in (anonymous)** customers
- **Loyalty points** with 4 membership tiers
- Manage **services, therapists, schedules**
- Admin dashboard 
- Email notifications
- REST APIs with Swagger (OpenAPI)

---

## ✨ Features

### 🔐 Authentication & Authorization
- [x] JWT-based authentication (stateless)
- [x] Role-based access control (USER, ADMIN, SUPER_ADMIN)
- [x] Email verification
- [x] Refresh token mechanism

### 📅 Booking Management
- [x] **Anonymous Booking:** 
- [x] **User Booking:** 
- [x] **Concurrent Booking Prevention:** 
- [x] **Booking Status Flow:** PENDING → CONFIRMED → COMPLETED/CANCELLED/NO_SHOW

### 🎁 Loyalty Program
- [x] **4 Membership Tiers:** BRONZE → SILVER → GOLD → PLATINUM
- [x] **Points Earning:** 10 points per 100,000 VND spent
- [x] **Points Redemption:** 100 points = 10,000 VND discount
- [x] **Transaction History:** Audit trail đầy đủ

### 👨‍💼 Admin Dashboard
- [x] **Real-time Statistics:** Today's bookings, revenue, pending approvals
- [x] **Revenue Charts:** Daily/Weekly/Monthly breakdown
- [x] **Booking Management:** Approve, confirm, complete, cancel
- [x] **User Management:** Verify, activate/deactivate, adjust points
- [x] **Service Management:** CRUD operations, pricing updates
- [x] **Therapist Management:** Assign services, skill levels

### 📧 Notifications
- [x] Email confirmation khi đặt lịch
- [x] Email cancellation notification
- [x] Email tier upgrade notification
- [x] Async processing với @Async

### 🔒 Security Features
- [x] BCrypt password hashing
- [x] JWT with expiry (1h access, 7d refresh)
- [x] Input validation với Bean Validation

---

## 🛠️ Tech Stack

### Backend
- **Java 17** - Programming language
- **Spring Boot 3.2.x** - Framework
  - Spring Web (REST APIs)
  - Spring Data JPA (Database ORM)
  - Spring Security (Authentication & Authorization)
  - Spring Validation (Input validation)
  - Spring Mail (Email notifications)
- **PostgreSQL 15** - Primary database
- **Lombok** - Reduce boilerplate code

### Security
- **JWT (jjwt 0.12.x)** - Token-based authentication
- **BCrypt** - Password hashing

### Documentation
- **SpringDoc OpenAPI 3** - API documentation (Swagger UI)

### DevOps & Tools
- **Maven** - Build tool
- **Docker** - Containerization
- **Git** - Version control
- **Postman** - API testing

---

### Core Tables (9 tables)

- `users`
- `bookings`
- `services`
- `therapists`
- `therapist_services`
- `loyalty_transactions`
- `activity_logs`
- `verification_tokens`
- `admins`
  
### Entity Relationships
```
users (1) ──────── (N) bookings
users (1) ──────── (N) loyalty_transactions
services (1) ─────── (N) bookings
therapists (1) ───── (N) bookings
therapists (N) ───── (M) services (through therapist_services)
bookings (1) ──────── (N) activity_logs
bookings (1) ──────── (N) loyalty_transactions
users (1) ──────── (N) verification_tokens
```

---


## 📖 API Documentation

### Swagger UI

Access interactive API documentation at:
```
http://localhost:8080/swagger-ui.html
```

### Quick API Overview

#### 🔐 Authentication Endpoints
```http
POST   /api/auth/register          # Register new user
POST   /api/auth/login             # Login (get JWT token)
POST   /api/auth/refresh-token     # Refresh access token
GET    /api/auth/verify-email      # Verify email
POST   /api/auth/forgot-password   # Request password reset
POST   /api/auth/reset-password    # Reset password
```

#### 📅 Public Booking Endpoints
```http
GET    /api/services                          # Get all services
GET    /api/services/{id}                     # Get service details
GET    /api/services/{id}/available-slots    # Get available time slots
POST   /api/bookings/anonymous                # Create anonymous booking
GET    /api/bookings/{bookingId}/status      # Check booking status
```

#### 👤 User Endpoints (Requires JWT)
```http
POST   /api/bookings/user            # Create booking with points
GET    /api/bookings/user             # Get my bookings
GET    /api/bookings/user/{id}        # Get booking detail
PUT    /api/bookings/user/{id}/cancel # Cancel booking
GET    /api/user/profile              # Get my profile
PUT    /api/user/profile              # Update profile
GET    /api/user/loyalty              # Get loyalty summary
```

#### 👨‍💼 Admin Endpoints (Requires ADMIN role)
```http
GET    /api/admin/dashboard/stats           # Dashboard statistics
GET    /api/admin/dashboard/today           # Today's summary
GET    /api/admin/bookings                  # All bookings (paginated)
GET    /api/admin/bookings/{id}             # Booking detail
PUT    /api/admin/bookings/{id}/status      # Update booking status
POST   /api/admin/bookings                  # Create booking by admin
GET    /api/admin/users                     # All users
GET    /api/admin/users/{id}                # User detail
POST   /api/admin/users/{id}/points         # Adjust points
GET    /api/admin/services                  # All services (+ inactive)
POST   /api/admin/services                  # Create service
PUT    /api/admin/services/{id}             # Update service
DELETE /api/admin/services/{id}             # Deactivate service
GET    /api/admin/therapists                # All therapists
POST   /api/admin/therapists                # Create therapist
POST   /api/admin/therapists/{id}/services  # Assign service
```

### Example API Calls

#### Register User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Nguyễn Văn A",
    "email": "user@example.com",
    "phone": "0905123456",
    "password": "User@123",
    "confirmPassword": "User@123",
    "dateOfBirth": "1995-05-15",
    "gender": "MALE"
  }'
```

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "User@123"
  }'
```

Response:
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "user": {
      "id": 1,
      "fullName": "Nguyễn Văn A",
      "email": "user@example.com",
      "loyaltyPoints": 0,
      "membershipTier": "BRONZE"
    }
  }
}
```
> Detailed schema: `scripts/init.sql` (or your schema file)
---
##📞 Contact

Developer: Thai Trong Tin
GitHub: https://github.com/tintt362

Email: tintt362@gmail.com
