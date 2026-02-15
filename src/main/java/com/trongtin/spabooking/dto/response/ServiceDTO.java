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
public class ServiceDTO {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private Integer durationMinutes;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private String imageUrl;
    private String category;
    private Integer displayOrder;
}