package com.trongtin.spabooking.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotDTO {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate bookingDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime bookingTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private ServiceDTO service;
    private TherapistDTO therapist;

    private Boolean isBooked;
    private Boolean isBlocked;
    private String blockReason;

    private Long bookingId;
    private String bookingStatus;
}
