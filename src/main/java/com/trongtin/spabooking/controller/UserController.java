package com.trongtin.spabooking.controller;

import com.trongtin.spabooking.dto.request.*;
import com.trongtin.spabooking.dto.response.ApiResponse;
import com.trongtin.spabooking.dto.response.*;
import com.trongtin.spabooking.service.BookingService;
import com.trongtin.spabooking.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BookingService bookingService;


    //GET /api/user/profile
    // Get user profile
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UserDTO profile = userService.getProfile(userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.success(profile));
    }


    //PUT /api/user/profile
    //Update profile

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserDTO updated = userService.updateProfile(userDetails.getUsername(), request);

        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật thông tin thành công!", updated)
        );
    }


    //  PUT /api/user/password
    // Change password
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(userDetails.getUsername(), request);

        return ResponseEntity.ok(
                ApiResponse.success("Đổi mật khẩu thành công!")
        );
    }


     //POST /api/user/bookings
     // Create booking (authenticated user)

    @PostMapping("/bookings")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserBookingRequest request
    ) {
        BookingResponse response = bookingService.createUserBooking(
                request,
                userDetails.getUsername()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đặt lịch thành công!", response));
    }

    //GET /api/user/bookings
     // Get user's bookings
    @Tag(name = "Bookings - User")
    @Operation(
            summary = "Get my bookings",
            description = "Retrieve all bookings for the authenticated user",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookings(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<BookingResponse> bookings = bookingService.getUserBookings(
                userDetails.getUsername()
        );

        return ResponseEntity.ok(ApiResponse.success(bookings));
    }


      //GET /api/user/bookings/{id}
     // Get booking details

    @Tag(name = "Bookings - User")
    @Operation(
            summary = "Get my bookings",
            description = "Retrieve all bookings for the authenticated user",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/bookings/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(
            @PathVariable Long id
    ) {
        BookingResponse booking = bookingService.getBookingById(id);

        return ResponseEntity.ok(ApiResponse.success(booking));
    }


     // PUT /api/user/bookings/{id}/cancel
     // Cancel booking
     @Tag(name = "Bookings - User")
     @Operation(
             summary = "Cancel booking",
             description = """
            Cancel a booking. Points are refunded if applicable.
            
            **Cancellation Policy:**
            - Must cancel at least 2 hours before booking time
            - Cannot cancel COMPLETED or NO_SHOW bookings
            - Refund processed within 24 hours for paid bookings
            """,
             security = @SecurityRequirement(name = "Bearer Authentication")
     )
     @ApiResponses(value = {
             @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking cancelled successfully"),
             @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cannot cancel (too late, already completed, etc.)"),
             @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not authorized to cancel this booking"),
             @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking not found")
     })
    @PutMapping("/bookings/{id}/cancel")
    public ResponseEntity<ApiResponse<String>> cancelBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody CancelBookingRequest request
    ) {
        bookingService.cancelBooking(id, userDetails.getUsername(), request);

        return ResponseEntity.ok(
                ApiResponse.success("Hủy lịch thành công!")
        );
    }
}