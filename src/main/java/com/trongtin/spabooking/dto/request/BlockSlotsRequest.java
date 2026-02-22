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
public class BlockSlotsRequest {

    @NotNull
    private Long therapistId;

    @NotNull
    @Future
    private LocalDate startDate;

    @NotNull
    @Future
    private LocalDate endDate;

    @Pattern(regexp = "^([01][0-9]|2[0-3]):[0-5][0-9]$")
    private String startTime; // null = all day

    @Pattern(regexp = "^([01][0-9]|2[0-3]):[0-5][0-9]$")
    private String endTime; // null = all day

    @NotBlank
    @Size(min = 10, max = 500)
    private String reason;

    private List<Long> serviceIds; // null = all services
}