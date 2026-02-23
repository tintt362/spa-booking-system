package com.trongtin.spabooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueChartDTO {

    private List<RevenueDataPoint> dataPoints;
    private String groupBy; // DAILY, WEEKLY, MONTHLY
    private LocalDate startDate;
    private LocalDate endDate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueDataPoint {
        private String label; // Date or period label
        private Long bookingCount;
        private java.math.BigDecimal revenue;
        private java.math.BigDecimal averageValue;
    }
}