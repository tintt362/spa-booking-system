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
- JWT authentication + refresh token
- Role-based access
- Email verification

### 📅 Booking
- Anonymous booking (no account required)
- User booking (earn/redeem points)
- Prevent double booking (pessimistic locking)
- Booking flow: `PENDING → CONFIRMED → COMPLETED / CANCELLED`

### 🎁 Loyalty Program
- 4 tiers: `BRONZE → SILVER → GOLD → PLATINUM`
- Earn points: **10 points / 100,000 VND**
- Redeem: **100 points = 10,000 VND discount**

### 👨‍💼 Admin
- Dashboard statistics (bookings, revenue, pending)
- Booking management: approve/confirm/complete/cancel
- CRUD users, services, therapists

### 📧 Notifications
- Booking confirmation / cancellation
- Async processing with `@Async`
- 
### 🧾 Activity Log
- Track actions on bookings (create/confirm/complete/cancel)
- Store actor info (ADMIN/STAFF/USER) and timestamps
- Keep history for debugging and audit purposes
---

## 🛠 Tech Stack

- **Java 17**
- **Spring Boot 3.2.x**
  - Spring Web, Spring Data JPA, Spring Security, Validation, Mail
- **PostgreSQL 15**
- **Lombok**, **MapStruct**
- **Swagger/OpenAPI**: SpringDoc
- **Testing**: JUnit 5, Mockito, Spring Boot Test

---

## 🗄 Database (Core Tables)

- `users`
- `bookings`
- `services`
- `therapists`
- `therapist_services`
- `loyalty_transactions`
- `activity_logs`
- `verification_tokens`
- `admins`

> Detailed schema: `scripts/init.sql` (or your schema file)

---
##📞 Contact

Developer: Thai Trong Tin
GitHub: https://github.com/tintt362

Email: tintt362@gmail.com
