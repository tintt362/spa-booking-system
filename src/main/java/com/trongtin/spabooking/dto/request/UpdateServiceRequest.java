package com.trongtin.spabooking.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateServiceRequest {

    @Size(min = 3, max = 100)
    private String name;

    @Size(max = 1000)
    private String description;

    @Min(value = 15)
    @Max(value = 300)
    private Integer durationMinutes;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    @DecimalMin(value = "0.0")
    private BigDecimal discountPrice;

    private String imageUrl;

    @Pattern(regexp = "^(MASSAGE|FACIAL|BODY_TREATMENT|SPA_PACKAGE)$")
    private String category;

    @Min(value = 0)
    @Max(value = 100)
    private Integer displayOrder;
}