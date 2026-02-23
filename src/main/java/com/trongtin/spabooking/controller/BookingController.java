package com.trongtin.spabooking.controller;

import com.trongtin.spabooking.dto.request.AnonymousBookingRequest;

import com.trongtin.spabooking.dto.response.BookingResponse;
import com.trongtin.spabooking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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


     //POST /api/bookings/anonymous
    // Create anonymous booking
     @Tag(name = "Bookings - Anonymous")
     @Operation(
             summary = "Create anonymous booking",
             description = """
            Create a booking without user account. Perfect for walk-in customers.
            
            **Business Rules:**
            - Cannot book on Sundays
            - Must book at least 2 hours in advance
            - Booking time must be :00 or :30 (e.g., 14:00, 14:30)
            - Working hours: 8:00 AM - 8:00 PM
            - Auto-assign therapist if not specified
            
            **Status Flow:**
            Anonymous bookings start with status PENDING and require admin confirmation.
            """
     )
     @ApiResponses(value = {
             @ApiResponse(
                     responseCode = "201",
                     description = "Booking created successfully",
                     content = @Content(
                             mediaType = "application/json",
                             schema = @Schema(implementation = BookingResponse.class),
                             examples = @ExampleObject(value = """
                    {
                        "success": true,
                        "data": {
                            "id": 1,
                            "bookingId": "BK20250220140000",
                            "customerName": "Nguyễn Văn A",
                            "customerPhone": "0905123456",
                            "customerEmail": "customer@example.com",
                            "serviceName": "Body Massage",
                            "therapistName": "Nguyễn Thị Mai",
                            "bookingDate": "2025-02-22",
                            "bookingTime": "14:00",
                            "endTime": "15:30",
                            "status": "PENDING",
                            "originalPrice": 500000,
                            "discountAmount": 0,
                            "finalPrice": 500000
                        }
                    }
                    """)
                     )
             ),
             @ApiResponse(
                     responseCode = "400",
                     description = "Validation error or business rule violation",
                     content = @Content(
                             mediaType = "application/json",
                             examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "errorCode": "VALIDATION_ERROR",
                        "message": "Invalid request data",
                        "errors": [
                            {
                                "field": "customerPhone",
                                "message": "Phone number must be valid Vietnamese format (10 digits starting with 0)",
                                "rejectedValue": "123"
                            }
                        ]
                    }
                    """)
                     )
             ),
             @ApiResponse(
                     responseCode = "404",
                     description = "Service or therapist not found"
             ),
             @ApiResponse(
                     responseCode = "409",
                     description = "Time slot not available",
                     content = @Content(
                             mediaType = "application/json",
                             examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "errorCode": "SLOT_NOT_AVAILABLE",
                        "message": "This time slot is no longer available. Please choose another time."
                    }
                    """)
                     )
             )
     })
    @PostMapping("/anonymous")
    public ResponseEntity<com.trongtin.spabooking.dto.response.ApiResponse<BookingResponse>> createAnonymousBooking(
            @Valid @RequestBody AnonymousBookingRequest request
    ) {
        BookingResponse response = bookingService.createAnonymousBooking(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(com.trongtin.spabooking.dto.response.ApiResponse.success(
                        "Đặt lịch thành công! Chúng tôi đã gửi thông tin xác nhận qua email/SMS.",
                        response
                ));
    }


     // GET /api/bookings/{bookingId}/status
     // Check booking status (for anonymous users)
     @Tag(name = "Bookings - Anonymous")
     @Operation(
             summary = "Check booking status",
             description = """
            Check booking status using booking ID.
            Booking ID format: BKYYYYMMDDHHmmss (e.g., BK20250220140000)
            
            Available to both anonymous and registered users.
            """
     )
     @ApiResponses(value = {
             @ApiResponse(responseCode = "200", description = "Booking found"),
             @ApiResponse(responseCode = "404", description = "Booking not found")
     })
    @GetMapping("/{bookingId}/status")
    public ResponseEntity<com.trongtin.spabooking.dto.response.ApiResponse<BookingResponse>> checkBookingStatus(
            @PathVariable String bookingId
    ) {
        BookingResponse response = bookingService.getBookingByBookingId(bookingId);

        return ResponseEntity.ok(com.trongtin.spabooking.dto.response.ApiResponse.success(response));
    }
}