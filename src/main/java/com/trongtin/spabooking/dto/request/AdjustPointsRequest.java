package com.trongtin.spabooking.dto.request;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjustPointsRequest {

    @NotNull
    @Min(value = -10000, message = "Không thể trừ quá 10,000 điểm")
    @Max(value = 10000, message = "Không thể cộng quá 10,000 điểm")
    private Integer points;

    @NotBlank
    @Size(min = 10, max = 500)
    private String reason;
}
