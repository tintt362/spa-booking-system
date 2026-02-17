package com.trongtin.spabooking.service;


import com.trongtin.spabooking.dto.request.AnonymousBookingRequest;
import com.trongtin.spabooking.dto.request.CancelBookingRequest;
import com.trongtin.spabooking.dto.request.UpdateBookingStatusRequest;
import com.trongtin.spabooking.dto.request.UserBookingRequest;
import com.trongtin.spabooking.dto.response.AvailableSlotDTO;
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
}