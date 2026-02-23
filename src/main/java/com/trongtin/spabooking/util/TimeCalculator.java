package com.trongtin.spabooking.util;


import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class TimeCalculator {

    /**
     * Calculate end time based on start time and duration
     */
    public LocalTime calculateEndTime(LocalTime startTime, Integer durationMinutes) {
        return startTime.plusMinutes(durationMinutes);
    }

    /**
     * Calculate time difference in hours
     */
    public long calculateHoursUntil(LocalDateTime target) {
        Duration duration = Duration.between(LocalDateTime.now(), target);
        return duration.toHours();
    }

    /**
     * Check if booking time is in valid range
     */
    public boolean isValidBookingTime(LocalTime time) {
        LocalTime openTime = LocalTime.of(8, 0);
        LocalTime closeTime = LocalTime.of(20, 0);
        return !time.isBefore(openTime) && time.isBefore(closeTime);
    }
}