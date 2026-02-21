package com.trongtin.spabooking.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodaySummaryDTO {

    private Long todayBookings;
    private Long upcomingBookings;
    private Long completedToday;
    private BigDecimal todayRevenue;
    private Long pendingApprovals;
    private Long activeUsers;
}
