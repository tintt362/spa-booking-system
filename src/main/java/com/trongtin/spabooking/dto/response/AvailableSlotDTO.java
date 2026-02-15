package com.trongtin.spabooking.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSlotDTO {

    @JsonFormat(pattern = "HH:mm")
    private LocalTime time;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private Boolean available;
    private TherapistDTO therapist;
    private String reason;
}