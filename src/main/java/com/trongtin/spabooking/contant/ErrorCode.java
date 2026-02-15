package com.trongtin.spabooking.contant;

public class ErrorCode {

    // Authentication
    public static final String AUTH_001 = "AUTH_001"; // Email already exists
    public static final String AUTH_002 = "AUTH_002"; // Phone already exists
    public static final String AUTH_003 = "AUTH_003"; // Invalid credentials
    public static final String AUTH_004 = "AUTH_004"; // Account not verified
    public static final String AUTH_005 = "AUTH_005"; // Account locked
    public static final String AUTH_006 = "AUTH_006"; // Invalid token
    public static final String AUTH_007 = "AUTH_007"; // Token expired
    public static final String AUTH_008 = "AUTH_008"; // Invalid JWT

    // Booking
    public static final String BOOK_001 = "BOOK_001"; // Service not found
    public static final String BOOK_002 = "BOOK_002"; // Therapist not found
    public static final String BOOK_003 = "BOOK_003"; // Date in past
    public static final String BOOK_004 = "BOOK_004"; // Invalid time format
    public static final String BOOK_005 = "BOOK_005"; // Slot not available
    public static final String BOOK_006 = "BOOK_006"; // Cannot book in past
    public static final String BOOK_007 = "BOOK_007"; // Advance booking exceeded
    public static final String BOOK_008 = "BOOK_008"; // Min advance hours
    public static final String BOOK_009 = "BOOK_009"; // Booking not found
    public static final String BOOK_010 = "BOOK_010"; // Unauthorized access
    public static final String BOOK_011 = "BOOK_011"; // Cannot cancel
    public static final String BOOK_012 = "BOOK_012"; // Already cancelled
    public static final String BOOK_013 = "BOOK_013"; // Already completed
    public static final String BOOK_014 = "BOOK_014"; // Insufficient points
    public static final String BOOK_015 = "BOOK_015"; // Therapist unavailable

    // Validation
    public static final String VAL_001 = "VAL_001"; // Invalid data

    // System
    public static final String SYS_001 = "SYS_001"; // Internal error
    public static final String SYS_002 = "SYS_002"; // Database error
}