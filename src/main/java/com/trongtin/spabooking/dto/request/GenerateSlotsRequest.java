package com.trongtin.spabooking.dto.request;


import jakarta.validation.constraints.*;
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
public class GenerateSlotsRequest {

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @Future(message = "Ngày bắt đầu phải là ngày trong tương lai")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @Future(message = "Ngày kết thúc phải là ngày trong tương lai")
    private LocalDate endDate;

    private List<Long> serviceIds; // null = all services

    private List<Long> therapistIds; // null = all therapists

    @Pattern(regexp = "^([01][0-9]|2[0-3]):[0-5][0-9]$")
    private String startTime; // default 08:00

    @Pattern(regexp = "^([01][0-9]|2[0-3]):[0-5][0-9]$")
    private String endTime; // default 20:00

    @Min(value = 15)
    @Max(value = 120)
    private Integer slotInterval; // minutes, default 30

    private Boolean skipWeekends = true;

    private Boolean overwriteExisting = false;
}
