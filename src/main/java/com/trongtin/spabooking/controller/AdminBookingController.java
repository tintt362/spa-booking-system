package com.trongtin.spabooking.controller;


import com.trongtin.spabooking.dto.request.AdminCreateBookingRequest;
import com.trongtin.spabooking.dto.request.UpdateBookingStatusRequest;
import com.trongtin.spabooking.dto.response.ApiResponse;
import com.trongtin.spabooking.dto.response.BookingDetailDTO;
import com.trongtin.spabooking.dto.response.BookingResponse;
import com.trongtin.spabooking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'STAFF')")
public class AdminBookingController {

    private final BookingService bookingService;


     // GET /api/admin/bookings
     //Get all bookings with filters

    @Tag(name = "Admin - Bookings")
    @Operation(
            summary = "Get all bookings (paginated)",
            description = """
            Retrieve all bookings with filtering and pagination.
            
            **Filter Options:**
            - status: Filter by booking status
            - date: Filter by specific date
            - phone: Search by customer phone
            - serviceId: Filter by service
            - therapistId: Filter by therapist
            
            **Pagination:**
            - Default page size: 20
            - Page is 0-indexed
            - Sortable by any field
            """
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getBookings(
            @RequestParam(required = false) String status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<BookingResponse> bookings = bookingService.getBookingsWithFilters(
                status,
                date,
                phone,
                serviceId,
                pageable
        );

        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

     //GET /api/admin/bookings/{id}
     // Get booking details with activity logs
     @Tag(name = "Admin - Bookings")
     @Operation(
             summary = "Get booking detail",
             description = "Get comprehensive booking information including history and logs"
     )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingDetailDTO>> getBookingDetail(
            @PathVariable Long id
    ) {
        BookingDetailDTO detail = bookingService.getBookingDetailForAdmin(id);

        return ResponseEntity.ok(ApiResponse.success(detail));
    }


    // PUT /api/admin/bookings/{id}/status
   //  Update booking status
    @Tag(name = "Admin - Bookings")
    @Operation(
            summary = "Update booking status",
            description = """
            Update booking status and related information.
            
            **Status Transitions:**
            - PENDING → CONFIRMED: Approve booking
            - CONFIRMED → COMPLETED: Mark as completed (award points)
            - CONFIRMED → CANCELLED: Cancel booking
            - CONFIRMED → NO_SHOW: Customer didn't show up
            
            **Additional Fields:**
            - adminNote: Internal note (not visible to customer)
            - paymentMethod: CASH, CARD, BANK_TRANSFER (required for COMPLETED)
            - paymentStatus: PAID, UNPAID (required for COMPLETED)
            """
    )
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> updateBookingStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookingStatusRequest request
    ) {
        bookingService.updateBookingStatus(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật trạng thái thành công!")
        );
    }


     // POST /api/admin/bookings
   //  Create booking for customer (by admin)
     @Tag(name = "Admin - Bookings")
     @Operation(
             summary = "Create booking by admin",
             description = """
            Create booking on behalf of customer (walk-in or phone booking).
            
            **Use Cases:**
            - Walk-in customers
            - Phone bookings
            - VIP customers
            
            **Features:**
            - Can auto-confirm (skip PENDING status)
            - Can add admin notes
            - Can specify payment method upfront
            """
     )
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponse>> createBookingForCustomer(
            @Valid @RequestBody AdminCreateBookingRequest request
    ) {
        BookingResponse response = bookingService.createBookingByAdmin(request);

        return ResponseEntity.ok(
                ApiResponse.success("Tạo booking thành công!", response)
        );
    }


     //GET /api/admin/bookings/export
     //Export bookings to Excel
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> exportBookings(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        String fileUrl = bookingService.exportBookingsToExcel(startDate, endDate);

        return ResponseEntity.ok(
                ApiResponse.success("Export thành công!", fileUrl)
        );
    }
}
