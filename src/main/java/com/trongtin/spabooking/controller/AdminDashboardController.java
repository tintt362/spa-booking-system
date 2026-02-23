package com.trongtin.spabooking.controller;


import com.trongtin.spabooking.dto.response.ApiResponse;
import com.trongtin.spabooking.dto.response.DashboardStatsDTO;
import com.trongtin.spabooking.dto.response.RevenueChartDTO;
import com.trongtin.spabooking.dto.response.TodaySummaryDTO;
import com.trongtin.spabooking.entity.RevenueGroupBy;
import com.trongtin.spabooking.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminDashboardController {

    private final StatisticsService statisticsService;


     //GET /api/admin/dashboard/stats
     // Get dashboard statistics

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getDashboardStats(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // Default: This month
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        DashboardStatsDTO stats = statisticsService.getDashboardStats(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

     // GET /api/admin/dashboard/today
     //Get today's summary

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<TodaySummaryDTO>> getTodaySummary() {
        TodaySummaryDTO summary = statisticsService.getTodaySummary();

        return ResponseEntity.ok(ApiResponse.success(summary));
    }


     //GET /api/admin/dashboard/revenue
     //Get revenue chart data
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<RevenueChartDTO>> getRevenueChart(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @RequestParam(defaultValue = "DAILY") String groupBy
    ) {
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        RevenueChartDTO chart = statisticsService.getRevenueChart(
                startDate,
                endDate,
                RevenueGroupBy.valueOf(groupBy)
        );

        return ResponseEntity.ok(ApiResponse.success(chart));
    }
}

