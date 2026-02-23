package com.trongtin.spabooking.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreateBookingRequest {

    @NotBlank
    @Size(min = 2, max = 100)
    private String customerName;

    @NotBlank
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$")
    private String customerPhone;

    @Email
    private String customerEmail;

    @NotNull
    @Positive
    private Long serviceId;

    @Positive
    private Long therapistId;

    @NotNull
    @Future
    private LocalDate bookingDate;

    @NotBlank
    @Pattern(regexp = "^([01][0-9]|2[0-3]):[0-5][0-9]$")
    private String bookingTime;

    @Size(max = 500)
    private String customerNote;

    @Size(max = 500)
    private String adminNote;

    @Pattern(regexp = "^(CASH|CARD|TRANSFER|WALLET)$")
    private String paymentMethod;

    private Boolean autoConfirm = false;
}