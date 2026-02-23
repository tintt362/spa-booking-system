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
public class CreateServiceRequest {

    @NotBlank(message = "Tên dịch vụ không được để trống")
    @Size(min = 3, max = 100)
    private String name;

    @NotBlank(message = "Slug không được để trống")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug chỉ chứa chữ thường, số và dấu gạch ngang")
    private String slug;

    @Size(max = 1000)
    private String description;

    @NotNull(message = "Thời gian không được để trống")
    @Min(value = 15, message = "Thời gian tối thiểu 15 phút")
    @Max(value = 300, message = "Thời gian tối đa 300 phút")
    private Integer durationMinutes;

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Giá khuyến mãi phải lớn hơn hoặc bằng 0")
    private BigDecimal discountPrice;

    private String imageUrl;

    @Pattern(regexp = "^(MASSAGE|FACIAL|BODY_TREATMENT|SPA_PACKAGE)$")
    private String category;

    @Min(value = 0)
    @Max(value = 100)
    private Integer displayOrder;
}