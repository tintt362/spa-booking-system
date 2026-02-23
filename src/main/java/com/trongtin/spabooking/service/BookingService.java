package com.trongtin.spabooking.service;


import com.trongtin.spabooking.dto.request.*;
import com.trongtin.spabooking.dto.response.AvailableSlotDTO;
import com.trongtin.spabooking.dto.response.BookingDetailDTO;
import com.trongtin.spabooking.dto.response.BookingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    // Anonymous booking
    BookingResponse createAnonymousBooking(AnonymousBookingRequest request);

    // User booking
    BookingResponse createUserBooking(UserBookingRequest request, String email);

    // Get bookings
    BookingResponse getBookingById(Long id);
    BookingResponse getBookingByBookingId(String bookingId);
    List<BookingResponse> getUserBookings(String email);
    Page<BookingResponse> getBookingsWithFilters(
            String status,
            LocalDate date,
            String phone,
            Long serviceId,
            Pageable pageable
    );

    // Cancel booking
    void cancelBooking(Long id, String email, CancelBookingRequest request);

    // Admin operations
    void updateBookingStatus(Long id, UpdateBookingStatusRequest request);

    // Get available slots
    List<AvailableSlotDTO> getAvailableSlots(Long serviceId, LocalDate date);


    BookingDetailDTO getBookingDetailForAdmin(Long id);

    BookingResponse createBookingByAdmin(AdminCreateBookingRequest adminCreateBookingRequest);

    String exportBookingsToExcel(LocalDate startDate, LocalDate endDate);
}