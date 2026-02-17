package com.trongtin.spabooking.util;


import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class BookingIdGenerator {

    private static final String PREFIX = "BK";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * Generate unique booking ID
     * Format: BK + yyyyMMddHHmmss
     * Example: BK20250213143022
     */
    public String generate() {
        return PREFIX + LocalDateTime.now().format(FORMATTER);
    }

    /**
     * Generate with custom prefix
     */
    public String generate(String prefix) {
        return prefix + LocalDateTime.now().format(FORMATTER);
    }
}