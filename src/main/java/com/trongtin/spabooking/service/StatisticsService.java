package com.trongtin.spabooking.service;



import com.trongtin.spabooking.dto.response.DashboardStatsDTO;
import com.trongtin.spabooking.entity.*;
import com.trongtin.spabooking.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;


    //Get dashboard statistics
    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats(LocalDate startDate, LocalDate endDate) {
        log.info("Getting dashboard stats: {} to {}", startDate, endDate);

        // Get all bookings in range
        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(b -> !b.getBookingDate().isBefore(startDate))
                .filter(b -> !b.getBookingDate().isAfter(endDate))
                .toList();

        // Total bookings
        long totalBookings = bookings.size();

        // Count by status
        long pending = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.PENDING)
                .count();

        long confirmed = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .count();

        long completed = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .count();

        long cancelled = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELLED)
                .count();

        // Total revenue (completed only)
        BigDecimal totalRevenue = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .map(Booking::getFinalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // New users
        long newUsers = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt().toLocalDate().isAfter(startDate.minusDays(1)))
                .filter(u -> u.getCreatedAt().toLocalDate().isBefore(endDate.plusDays(1)))
                .count();

        // Popular services
        Map<String, Long> serviceStats = new HashMap<>();
        bookings.forEach(booking -> {
            String serviceName = booking.getService().getName();
            serviceStats.put(serviceName, serviceStats.getOrDefault(serviceName, 0L) + 1);
        });

        return DashboardStatsDTO.builder()
                .totalBookings(totalBookings)
                .pendingBookings(pending)
                .confirmedBookings(confirmed)
                .completedBookings(completed)
                .cancelledBookings(cancelled)
                .totalRevenue(totalRevenue)
                .newUsers(newUsers)
                .serviceStats(serviceStats)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }
}
