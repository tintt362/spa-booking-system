package com.trongtin.spabooking.service;


import com.trongtin.spabooking.dto.response.DashboardStatsDTO;
import com.trongtin.spabooking.dto.response.RevenueChartDTO;
import com.trongtin.spabooking.dto.response.TodaySummaryDTO;
import com.trongtin.spabooking.entity.*;
import com.trongtin.spabooking.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
        long  newUsers = userRepository.findAll().stream()
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


    // Get today's summary
    @Transactional(readOnly = true)
    public TodaySummaryDTO getTodaySummary() {
        LocalDate today = LocalDate.now();

        List<Booking> allBookings = bookingRepository.findAll();

        // Today's bookings
        long todayBookings = allBookings.stream()
                .filter(b -> b.getBookingDate().equals(today))
                .count();

        // Upcoming bookings (today and future, not cancelled)
        long upcomingBookings = allBookings.stream()
                .filter(b -> !b.getBookingDate().isBefore(today))
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .filter(b -> b.getStatus() != BookingStatus.COMPLETED)
                .count();

        // Completed today
        long completedToday = allBookings.stream()
                .filter(b -> b.getCompletedAt() != null)
                .filter(b -> b.getCompletedAt().toLocalDate().equals(today))
                .count();

        // Today's revenue (completed only)
        BigDecimal todayRevenue = allBookings.stream()
                .filter(b -> b.getCompletedAt() != null)
                .filter(b -> b.getCompletedAt().toLocalDate().equals(today))
                .map(Booking::getFinalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Pending approvals
        long pendingApprovals = allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.PENDING)
                .count();

        // Active users (logged in last 30 days)
        long activeUsers = userRepository.findAll().stream()
                .filter(u -> u.getLastLoginAt() != null)
                .filter(u -> u.getLastLoginAt().isAfter(
                        java.time.LocalDateTime.now().minusDays(30)
                ))
                .count();

        return TodaySummaryDTO.builder()
                .todayBookings(todayBookings)
                .upcomingBookings(upcomingBookings)
                .completedToday(completedToday)
                .todayRevenue(todayRevenue)
                .pendingApprovals(pendingApprovals)
                .activeUsers(activeUsers)
                .build();
    }


    // Get revenue chart data
    @Transactional(readOnly = true)
    public RevenueChartDTO getRevenueChart(
            LocalDate startDate,
            LocalDate endDate,
            RevenueGroupBy groupBy
    ) {
        log.info("Getting revenue chart: {} to {}, group by {}",
                startDate, endDate, groupBy);

        // Get all completed bookings in range
        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .filter(b -> b.getCompletedAt() != null)
                .filter(b -> {
                    LocalDate completedDate = b.getCompletedAt().toLocalDate();
                    return !completedDate.isBefore(startDate) &&
                            !completedDate.isAfter(endDate);
                })
                .toList();

        List<RevenueChartDTO.RevenueDataPoint> dataPoints = new ArrayList<>();

        switch (groupBy) {
            case DAILY -> dataPoints = generateDailyDataPoints(bookings, startDate, endDate);
            case WEEKLY -> dataPoints = generateWeeklyDataPoints(bookings, startDate, endDate);
            case MONTHLY -> dataPoints = generateMonthlyDataPoints(bookings, startDate, endDate);
        }

        return RevenueChartDTO.builder()
                .dataPoints(dataPoints)
                .groupBy(groupBy.name())
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }


    // Generate daily data points
    private List<RevenueChartDTO.RevenueDataPoint> generateDailyDataPoints(
            List<Booking> bookings,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<RevenueChartDTO.RevenueDataPoint> dataPoints = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            final LocalDate date = current;

            List<Booking> dayBookings = bookings.stream()
                    .filter(b -> b.getCompletedAt().toLocalDate().equals(date))
                    .toList();

            BigDecimal revenue = dayBookings.stream()
                    .map(Booking::getFinalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal avgValue = dayBookings.isEmpty()
                    ? BigDecimal.ZERO
                    : revenue.divide(
                    BigDecimal.valueOf(dayBookings.size()),
                    2,
                    RoundingMode.HALF_UP
            );

            dataPoints.add(RevenueChartDTO.RevenueDataPoint.builder()
                    .label(date.format(formatter))
                    .bookingCount((long) dayBookings.size())
                    .revenue(revenue)
                    .averageValue(avgValue)
                    .build());

            current = current.plusDays(1);
        }

        return dataPoints;
    }

    ///      Generate weekly data points
    private List<RevenueChartDTO.RevenueDataPoint> generateWeeklyDataPoints(
            List<Booking> bookings,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<RevenueChartDTO.RevenueDataPoint> dataPoints = new ArrayList<>();

        LocalDate weekStart = startDate;
        int weekNumber = 1;

        while (!weekStart.isAfter(endDate)) {
            LocalDate weekEnd = weekStart.plusDays(6);
            if (weekEnd.isAfter(endDate)) {
                weekEnd = endDate;
            }

            final LocalDate wStart = weekStart;
            final LocalDate wEnd = weekEnd;

            List<Booking> weekBookings = bookings.stream()
                    .filter(b -> {
                        LocalDate date = b.getCompletedAt().toLocalDate();
                        return !date.isBefore(wStart) && !date.isAfter(wEnd);
                    })
                    .toList();

            BigDecimal revenue = weekBookings.stream()
                    .map(Booking::getFinalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal avgValue = weekBookings.isEmpty()
                    ? BigDecimal.ZERO
                    : revenue.divide(
                    BigDecimal.valueOf(weekBookings.size()),
                    2,
                    RoundingMode.HALF_UP
            );

            dataPoints.add(RevenueChartDTO.RevenueDataPoint.builder()
                    .label("Tuần " + weekNumber)
                    .bookingCount((long) weekBookings.size())
                    .revenue(revenue)
                    .averageValue(avgValue)
                    .build());

            weekStart = weekStart.plusDays(7);
            weekNumber++;
        }

        return dataPoints;
    }


    ///  Generate monthly data points
    private List<RevenueChartDTO.RevenueDataPoint> generateMonthlyDataPoints(
            List<Booking> bookings,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<RevenueChartDTO.RevenueDataPoint> dataPoints = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");

        LocalDate current = startDate.withDayOfMonth(1);

        while (!current.isAfter(endDate)) {
            final int year = current.getYear();
            final int month = current.getMonthValue();

            List<Booking> monthBookings = bookings.stream()
                    .filter(b -> {
                        LocalDate date = b.getCompletedAt().toLocalDate();
                        return date.getYear() == year && date.getMonthValue() == month;
                    })
                    .toList();

            BigDecimal revenue = monthBookings.stream()
                    .map(Booking::getFinalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal avgValue = monthBookings.isEmpty()
                    ? BigDecimal.ZERO
                    : revenue.divide(
                    BigDecimal.valueOf(monthBookings.size()),
                    2,
                    RoundingMode.HALF_UP
            );

            dataPoints.add(RevenueChartDTO.RevenueDataPoint.builder()
                    .label(current.format(formatter))
                    .bookingCount((long) monthBookings.size())
                    .revenue(revenue)
                    .averageValue(avgValue)
                    .build());

            current = current.plusMonths(1);
        }

        return dataPoints;
    }

}
