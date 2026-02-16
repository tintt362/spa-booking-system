package com.trongtin.spabooking.service;

import com.trongtin.spabooking.contant.ErrorCode;
import com.trongtin.spabooking.dto.request.*;
import com.trongtin.spabooking.dto.response.*;
import com.trongtin.spabooking.entity.*;
import com.trongtin.spabooking.exception.BookingException;
import com.trongtin.spabooking.exception.ResourceNotFoundException;
import com.trongtin.spabooking.mapper.BookingMapper;
import com.trongtin.spabooking.repository.*;
import com.trongtin.spabooking.service.async.AsyncBookingService;
import com.trongtin.spabooking.service.validartor.*;
import com.trongtin.spabooking.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingSlotRepository slotRepository;
    private final ServiceRepository serviceRepository;
    private final TherapistRepository therapistRepository;
    private final TherapistServiceRepository therapistServiceRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;

    private final BookingValidator bookingValidator;
    private final BookingIdGenerator idGenerator;
    private final TimeCalculator timeCalculator;
    private final BookingMapper mapper;

    private final AsyncBookingService asyncBookingService; // ADD THIS

    // Create anonymous booking
    @Override
    @Transactional
    public BookingResponse createAnonymousBooking(AnonymousBookingRequest request) {
        log.info("Creating anonymous booking for phone: {}", request.getCustomerPhone());

        // STEP 1: Validate (30ms)
        bookingValidator.validateBookingRequest(request);

        // STEP 2: Get service
        com.trongtin.spabooking.entity.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service"));

        // STEP 3: Get therapist (if specified)
        Therapist therapist = null;
        if (request.getTherapistId() != null) {
            therapist = therapistRepository.findById(request.getTherapistId())
                    .orElseThrow(() -> new ResourceNotFoundException("Therapist"));
        }

        // STEP 4: Parse booking time
        LocalTime bookingTime = LocalTime.parse(request.getBookingTime());

        // STEP 5: Calculate end time
        LocalTime endTime = timeCalculator.calculateEndTime(
                bookingTime,
                service.getDurationMinutes()
        );
        log.info("Anonymous create booking: endTime ={}", endTime);

        // STEP 6: CRITICAL - Lock slot with Pessimistic Lock
        BookingSlot slot = slotRepository.findAvailableSlotForUpdate(
                        request.getServiceId(),
                        request.getTherapistId(),
                        request.getBookingDate(),
                        bookingTime
                )
                .orElseThrow(() -> new BookingException(
                        ErrorCode.BOOK_005,
                        "Khung giờ này đã được đặt. Vui lòng chọn giờ khác."
                ));

        log.debug("Slot locked: slotId={}", slot.getId());
        log.debug("Slot Object: slotId={}", slot.toString());

        // STEP 7: Create booking (40ms)
        Booking booking = Booking.builder()
                .bookingId(idGenerator.generate())
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .customerEmail(request.getCustomerEmail())
                .service(service)
                .therapist(therapist)
                .bookingDate(request.getBookingDate())
                .bookingTime(bookingTime)
                .endTime(endTime)
                .isAnonymous(true)
                .status(BookingStatus.PENDING)
                .originalPrice(service.getPrice())
                .discountAmount(java.math.BigDecimal.ZERO)
                .finalPrice(service.getPrice())
                .customerNote(request.getNote())
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created: bookingId={}", savedBooking.getBookingId());
        log.info("Booking Object: booking Object={}", savedBooking);

        // STEP 8: Mark slot as booked (20ms)
        slot.setIsBooked(true);
        slot.setBookingId(savedBooking.getId());
        slotRepository.save(slot);

        // STEP 9: Log activity (10ms)
        logActivity(
                savedBooking,
                "BOOKING_CREATED",
                "CUSTOMER",
                null,
                "Khách hàng tạo booking (ẩn danh)"
        );


        // Note: Async jobs (email, SMS) will be triggered
        // STEP 10: Trigger async jobs (NON-BLOCKING) (~10ms)
        asyncBookingService.sendBookingConfirmationEmail(savedBooking);
        asyncBookingService.notifyAdminNewBooking(savedBooking);

        log.info("Booking creation completed, async jobs triggered");
        log.info("Booking creation completed in ~{}ms", 200);
        return mapper.toResponse(savedBooking);
    }


    //Get available slots for a service on a specific date
    @Override
    @Transactional(readOnly = true)
    public List<AvailableSlotDTO> getAvailableSlots(Long serviceId, LocalDate date) {
        log.info("Getting available slots: serviceId={}, date={}", serviceId, date);

        // Validate service exists
        com.trongtin.spabooking.entity.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service"));

        // Get available slots
        List<BookingSlot> slots = slotRepository.findAvailableSlots(serviceId, date);

        return slots.stream()
                .map(slot -> AvailableSlotDTO.builder()
                        .time(slot.getBookingTime())
                        .endTime(slot.getEndTime())
                        .available(true)
                        .therapist(slot.getTherapist() != null
                                ? mapper.toTherapistDTO(slot.getTherapist())
                                : null)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Helper: Log activity
     */
    private void logActivity(
            Booking booking,
            String action,
            String actorType,
            Long actorId,
            String description
    ) {
        ActivityLog log = ActivityLog.builder()
                .booking(booking)
                .action(action)
                .actorType(actorType)
                .actorId(actorId)
                .description(description)
                .build();

        activityLogRepository.save(log);
    }


    @Override
    @Transactional
    public BookingResponse createUserBooking(UserBookingRequest request, String email) {
        log.info("Creating user booking for email: {}", email);

        // Get user
        User user = userRepository.findActiveByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User"));

        // Validate service
        com.trongtin.spabooking.entity.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service"));

        // Validate therapist if specified
        Therapist therapist = null;
        if (request.getTherapistId() != null) {
            therapist = therapistRepository.findById(request.getTherapistId())
                    .orElseThrow(() -> new ResourceNotFoundException("Therapist"));

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

        // Parse booking time
        LocalTime bookingTime = LocalTime.parse(request.getBookingTime());

        // Validate booking datetime
        bookingValidator.validateBookingDateTime(
                request.getBookingDate(),
                bookingTime,
                service
        );

        // Calculate end time
        LocalTime endTime = timeCalculator.calculateEndTime(
                bookingTime,
                service.getDurationMinutes()
        );
        log.info("User create booking: endTime ={}", endTime);
        // CRITICAL: Lock slot
        BookingSlot slot = slotRepository.findAvailableSlotForUpdate(
                        request.getServiceId(),
                        request.getTherapistId(),
                        request.getBookingDate(),
                        bookingTime
                )
                .orElseThrow(() -> new BookingException(
                        ErrorCode.BOOK_005,
                        "Khung giờ này đã được đặt"
                ));

        // Calculate pricing with points redemption
        java.math.BigDecimal originalPrice = service.getPrice();
        java.math.BigDecimal discountAmount = java.math.BigDecimal.ZERO;

        if (Boolean.TRUE.equals(request.getUsePoints()) &&
                request.getPointsToRedeem() != null &&
                request.getPointsToRedeem() > 0) {

            // Validate points
            if (request.getPointsToRedeem() > user.getLoyaltyPoints()) {
                throw new BookingException(
                        ErrorCode.BOOK_014,
                        "Số điểm không đủ"
                );
            }

            // 100 points = 10,000 VND discount
            discountAmount = java.math.BigDecimal.valueOf(
                    request.getPointsToRedeem() * 100L
            );

            // Cannot exceed original price
            if (discountAmount.compareTo(originalPrice) > 0) {
                discountAmount = originalPrice;
            }

            // Deduct points
            user.setLoyaltyPoints(user.getLoyaltyPoints() - request.getPointsToRedeem());
            userRepository.save(user);
        }

        java.math.BigDecimal finalPrice = originalPrice.subtract(discountAmount);

        // Create booking
        Booking booking = Booking.builder()
                .bookingId(idGenerator.generate())
                .customerName(user.getFullName())
                .customerPhone(user.getPhone())
                .customerEmail(user.getEmail())
                .user(user)
                .service(service)
                .therapist(therapist)
                .bookingDate(request.getBookingDate())
                .bookingTime(bookingTime)
                .endTime(endTime)
                .isAnonymous(false)
                .status(BookingStatus.PENDING)
                .originalPrice(originalPrice)
                .discountAmount(discountAmount)
                .finalPrice(finalPrice)
                .customerNote(request.getNote())
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        // Mark slot as booked
        slot.setIsBooked(true);
        slot.setBookingId(savedBooking.getId());
        slotRepository.save(slot);

        // Log activity
        logActivity(
                savedBooking,
                "BOOKING_CREATED",
                "CUSTOMER",
                user.getId(),
                "Khách hàng tạo booking (đã đăng nhập)"
        );

        log.info("User booking created: bookingId={}", savedBooking.getBookingId());
        return mapper.toResponse(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking"));

        return mapper.toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingByBookingId(String bookingId) {
        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking"));

        return mapper.toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookings(String email) {
        User user = userRepository.findActiveByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User"));

        List<Booking> bookings = bookingRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId());

        return bookings.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getBookingsWithFilters(
            String statusStr,
            LocalDate date,
            String phone,
            Long serviceId,
            Pageable pageable
    ) {
        BookingStatus status = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = BookingStatus.valueOf(statusStr);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status: {}", statusStr);
            }
        }

        Page<Booking> bookings = bookingRepository.findWithFilters(
                status,
                date,
                phone,
                serviceId,
                pageable
        );

        return bookings.map(mapper::toResponse);
    }

    @Override
    @Transactional
    public void cancelBooking(Long id, String email, CancelBookingRequest request) {
        log.info("Cancelling booking: id={}, email={}", id, email);

        // Get booking
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking"));

        // Ownership check
        if (booking.getUser() == null ||
                !booking.getUser().getEmail().equals(email)) {
            throw new BookingException(
                    ErrorCode.BOOK_010,
                    "Bạn không có quyền hủy booking này"
            );
        }

        // Status check
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingException(
                    ErrorCode.BOOK_012,
                    "Booking đã bị hủy"
            );
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BookingException(
                    ErrorCode.BOOK_013,
                    "Booking đã hoàn thành, không thể hủy"
            );
        }

        // Time validation
        bookingValidator.validateCancellation(
                booking.getBookingDate(),
                booking.getBookingTime()
        );

        // Calculate refund
        java.math.BigDecimal refundAmount = booking.getDiscountAmount();

        if (refundAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
            // Refund points
            int pointsToRefund = refundAmount.divide(
                    java.math.BigDecimal.valueOf(100),
                    0,
                    java.math.RoundingMode.HALF_UP
            ).intValue();

            User user = booking.getUser();
            user.setLoyaltyPoints(user.getLoyaltyPoints() + pointsToRefund);
            userRepository.save(user);

            log.info("Refunded {} points to user {}", pointsToRefund, user.getId());
        }

        // Update booking
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(request.getReason());
        booking.setCancelledAt(java.time.LocalDateTime.now());
        bookingRepository.save(booking);

        // Release slot
        BookingSlot slot = slotRepository.findByBookingId(id)
                .orElseThrow(() -> new ResourceNotFoundException("BookingSlot"));

        slot.setIsBooked(false);
        slot.setBookingId(null);
        slotRepository.save(slot);
        // Trigger async email
        asyncBookingService.sendCancellationEmail(booking);

        log.info("Booking cancelled: bookingId={}", booking.getBookingId());
        // Log activity
        logActivity(
                booking,
                "BOOKING_CANCELLED",
                "CUSTOMER",
                booking.getUser().getId(),
                "Lý do: " + request.getReason()
        );

        log.info("Booking cancelled: bookingId={}", booking.getBookingId());
    }

    @Override
    @Transactional
    public void updateBookingStatus(Long id, UpdateBookingStatusRequest request) {
        log.info("Updating booking status: id={}, newStatus={}", id, request.getStatus());

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking"));

        BookingStatus oldStatus = booking.getStatus();
        BookingStatus newStatus = BookingStatus.valueOf(request.getStatus());

        // Update status
        booking.setStatus(newStatus);

        // Update timestamps
        switch (newStatus) {
            case CONFIRMED:
                booking.setConfirmedAt(java.time.LocalDateTime.now());
                break;
            case COMPLETED:
                booking.setCompletedAt(java.time.LocalDateTime.now());

                // Update payment info
                if (request.getPaymentMethod() != null) {
                    booking.setPaymentMethod(
                            PaymentMethod.valueOf(request.getPaymentMethod())
                    );
                }
                if (request.getPaymentStatus() != null) {
                    booking.setPaymentStatus(
                            PaymentStatus.valueOf(request.getPaymentStatus())
                    );
                }
                break;
            case CANCELLED:
                booking.setCancelledAt(java.time.LocalDateTime.now());

                // Release slot
                BookingSlot slot = slotRepository.findByBookingId(id).orElse(null);
                if (slot != null) {
                    slot.setIsBooked(false);
                    slot.setBookingId(null);
                    slotRepository.save(slot);
                }
                break;
            default:
                break;
        }

        // Update admin note
        if (request.getAdminNote() != null) {
            booking.setAdminNote(request.getAdminNote());
        }

        bookingRepository.save(booking);

        // Log activity
        logActivity(
                booking,
                "STATUS_CHANGED",
                "ADMIN",
                null,
                String.format("Thay đổi: %s → %s", oldStatus, newStatus)
        );

        log.info("Booking status updated: bookingId={}", booking.getBookingId());
    }
}
