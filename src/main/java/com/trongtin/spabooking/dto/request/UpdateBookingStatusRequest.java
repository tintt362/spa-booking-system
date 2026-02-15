package com.trongtin.spabooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookingStatusRequest {

    @NotBlank(message = "Vui lòng chọn trạng thái")
    @Pattern(
            regexp = "^(CONFIRMED|COMPLETED|CANCELLED|NO_SHOW)$",
            message = "Trạng thái không hợp lệ"
    )
    private String status;

    @Size(max = 500)
    private String adminNote;

    @Pattern(regexp = "^(CASH|CARD|TRANSFER|WALLET)$")
    private String paymentMethod;

    @Pattern(regexp = "^(PAID|UNPAID|REFUNDED)$")
    private String paymentStatus;
}