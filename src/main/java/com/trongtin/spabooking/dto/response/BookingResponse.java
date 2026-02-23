package com.trongtin.spabooking.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private String bookingId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;

    private ServiceDTO service;
    private TherapistDTO therapist;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate bookingDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime bookingTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private String status;
    private Boolean isAnonymous;

    private BigDecimal originalPrice;
    private BigDecimal discountAmount;
    private BigDecimal finalPrice;

    private String paymentStatus;
    private String paymentMethod;

    private String customerNote;
    private String adminNote;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private Boolean canCancel;
    private Boolean canReview;
}
