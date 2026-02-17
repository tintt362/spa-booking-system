package com.trongtin.spabooking.controller;

import com.trongtin.spabooking.dto.request.AnonymousBookingRequest;
import com.trongtin.spabooking.dto.response.*;
import com.trongtin.spabooking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private final BookingService bookingService;

    /**
     * POST /api/bookings/anonymous
     * Create anonymous booking
     */
    @PostMapping("/anonymous")
    public ResponseEntity<ApiResponse<BookingResponse>> createAnonymousBooking(
            @Valid @RequestBody AnonymousBookingRequest request
    ) {
        BookingResponse response = bookingService.createAnonymousBooking(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Đặt lịch thành công! Chúng tôi đã gửi thông tin xác nhận qua email/SMS.",
                        response
                ));
    }

    /**
     * GET /api/bookings/{bookingId}/status
     * Check booking status (for anonymous users)
     */
    @GetMapping("/{bookingId}/status")
    public ResponseEntity<ApiResponse<BookingResponse>> checkBookingStatus(
            @PathVariable String bookingId
    ) {
        BookingResponse response = bookingService.getBookingByBookingId(bookingId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}