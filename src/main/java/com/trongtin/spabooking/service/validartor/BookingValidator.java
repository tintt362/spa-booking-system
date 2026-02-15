package com.trongtin.spabooking.service.validartor;



import com.trongtin.spabooking.contant.ErrorCode;
import com.trongtin.spabooking.dto.request.*;
import com.trongtin.spabooking.entity.*;
import com.trongtin.spabooking.exception.BookingException;
import com.trongtin.spabooking.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingValidator {

    private final ServiceRepository serviceRepository;
    private final TherapistRepository therapistRepository;
    private final TherapistServiceRepository therapistServiceRepository;

    /**
     * Validate anonymouse booking request
     */
    public void validateBookingRequest(AnonymousBookingRequest request) {
        // 1. Validate service exists
        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new BookingException(
                        ErrorCode.BOOK_001,
                        "Dịch vụ không tồn tại"
                ));

        if (!service.getIsActive()) {
            throw new BookingException(
                    ErrorCode.BOOK_001,
                    "Dịch vụ hiện không khả dụng"
            );
        }

        // 2. Validate therapist if provided
        if (request.getTherapistId() != null) {
            Therapist therapist = therapistRepository.findById(request.getTherapistId())
                    .orElseThrow(() -> new BookingException(
                            ErrorCode.BOOK_002,
                            "Therapist không tồn tại"
                    ));

            if (!therapist.getIsActive()) {
                throw new BookingException(
                        ErrorCode.BOOK_015,
                        "Therapist hiện không khả dụng"
                );
            }

            // Check if therapist can do this service
            boolean canDoService = therapistServiceRepository
                    .existsByTherapistIdAndServiceId(
                            request.getTherapistId(),
                            request.getServiceId()
                    );

            if (!canDoService) {
                throw new BookingException(
                        ErrorCode.BOOK_015,
                        "Therapist này không thể thực hiện dịch vụ bạn chọn"
                );
            }
        }

        // 3. Validate date and time
        LocalTime bookingTime = LocalTime.parse(request.getBookingTime());
        validateBookingDateTime(
                request.getBookingDate(),
                bookingTime,
                service
        );
    }


    //  Validate booking date and time
    public void validateBookingDateTime(
            LocalDate bookingDate,
            LocalTime bookingTime,
            Service service
    ) {
        LocalDateTime bookingDateTime = LocalDateTime.of(bookingDate, bookingTime);
        LocalDateTime now = LocalDateTime.now();

        // 1. Check not in past
        if (bookingDateTime.isBefore(now)) {
            throw new BookingException(
                    ErrorCode.BOOK_006,
                    "Không thể đặt lịch trong quá khứ"
            );
        }

        // 2. Check minimum advance hours (2 hours)
        if (bookingDateTime.isBefore(now.plusHours(2))) {
            throw new BookingException(
                    ErrorCode.BOOK_008,
                    "Vui lòng đặt trước ít nhất 2 giờ"
            );
        }

        // 3. Check maximum advance days (30 days)
        if (bookingDateTime.isAfter(now.plusDays(30))) {
            throw new BookingException(
                    ErrorCode.BOOK_007,
                    "Chỉ có thể đặt trước tối đa 30 ngày"
            );
        }

        // 4. Check business hours (8:00 - 20:00)
        if (bookingTime.isBefore(LocalTime.of(8, 0)) ||
                bookingTime.isAfter(LocalTime.of(20, 0))) {
            throw new BookingException(
                    ErrorCode.BOOK_004,
                    "Giờ làm việc từ 8:00 đến 20:00"
            );
        }

        // 5. Check end time doesn't exceed closing time
        LocalTime endTime = bookingTime.plusMinutes(service.getDurationMinutes());
        if (endTime.isAfter(LocalTime.of(20, 0))) {
            throw new BookingException(
                    ErrorCode.BOOK_004,
                    "Dịch vụ sẽ kết thúc sau giờ đóng cửa (20:00)"
            );
        }
    }

 // Validate cancellation
    public void validateCancellation(LocalDate bookingDate, LocalTime bookingTime) {
        LocalDateTime bookingDateTime = LocalDateTime.of(bookingDate, bookingTime);
        LocalDateTime now = LocalDateTime.now();

        long hoursUntil = java.time.Duration.between(now, bookingDateTime).toHours();

        if (hoursUntil < 2) {
            throw new BookingException(
                    ErrorCode.BOOK_011,
                    "Không thể hủy lịch trong vòng 2 giờ trước giờ hẹn"
            );
        }
    }
}