package com.trongtin.spabooking.controller;


import com.trongtin.spabooking.dto.response.ApiResponse;
import com.trongtin.spabooking.dto.response.DashboardStatsDTO;
import com.trongtin.spabooking.dto.response.RevenueChartDTO;
import com.trongtin.spabooking.dto.response.TodaySummaryDTO;
import com.trongtin.spabooking.entity.RevenueGroupBy;
import com.trongtin.spabooking.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @Tag(name = "Admin - Dashboard")
    @Operation(
            summary = "Get dashboard statistics",
            description = """
            Get comprehensive dashboard statistics for date range.
            
            **Includes:**
            - Total bookings (all statuses)
            - Completed bookings
            - Total revenue
            - Average booking value
            - Pending approvals
            - Today's bookings
            - Upcoming bookings (next 7 days)
            - No-show rate
            - Cancellation rate
            - Top services
            - Top therapists
            """,
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Statistics retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not authorized (requires ADMIN role)")
    })
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
     @Tag(name = "Admin - Dashboard")
     @Operation(
             summary = "Get today's summary",
             description = "Quick overview of today's bookings and pending actions"
     )
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<TodaySummaryDTO>> getTodaySummary() {
        TodaySummaryDTO summary = statisticsService.getTodaySummary();

        return ResponseEntity.ok(ApiResponse.success(summary));
    }


     //GET /api/admin/dashboard/revenue
     //Get revenue chart data
     @Tag(name = "Admin - Dashboard")
     @Operation(
             summary = "Get revenue chart data",
             description = """
            Get revenue data for charts and graphs.
            
            **Group By Options:**
            - DAILY: Day-by-day revenue
            - WEEKLY: Week-by-week revenue
            - MONTHLY: Month-by-month revenue
            """
     )
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

