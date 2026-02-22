package com.trongtin.spabooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotStatisticsDTO {

    private Long totalSlots;
    private Long bookedSlots;
    private Long blockedSlots;
    private Long availableSlots;

    private Double utilizationRate; // Percentage

    private Map<String, Long> slotsByService;
    private Map<String, Long> slotsByTherapist;
    private Map<String, Double> utilizationByService;
}