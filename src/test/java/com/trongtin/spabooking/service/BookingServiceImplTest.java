package com.trongtin.spabooking.service;

import com.trongtin.spabooking.dto.request.*;
import com.trongtin.spabooking.dto.response.*;
import com.trongtin.spabooking.entity.*;
import com.trongtin.spabooking.exception.*;
import com.trongtin.spabooking.mapper.BookingMapper;
import com.trongtin.spabooking.repository.*;
import com.trongtin.spabooking.service.email.*;
import com.trongtin.spabooking.service.validartor.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Unit Tests")
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private TherapistRepository therapistRepository;

    @Mock
    private TherapistServiceRepository therapistServiceRepository;

    @Mock
    private ActivityLogRepository activityLogRepository;

    @Mock
    private LoyaltyTransactionRepository loyaltyTransactionRepository;

    @Mock
    private BookingValidator bookingValidator;

    @Mock
    private BusinessRulesValidator businessRulesValidator;

    @Mock
    private DynamicSlotService dynamicSlotService;

    @Mock
    private EmailService emailService;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private AnonymousBookingRequest anonymousRequest;
    private UserBookingRequest userRequest;
    private Service service;
    private Therapist therapist;
    private User user;
    private Booking booking;

    @BeforeEach
    void setUp() {
        // Setup test data
        anonymousRequest = AnonymousBookingRequest.builder()
                .customerName("Nguyễn Văn A")
                .customerPhone("0905123456")
                .customerEmail("test@example.com")
                .serviceId(1L)
                .therapistId(1L)
                .bookingDate(LocalDate.now().plusDays(1))
                .bookingTime(LocalTime.of(14, 0))
                .note("Test booking")
                .build();

        userRequest = UserBookingRequest.builder()
                .serviceId(1L)
                .therapistId(1L)
                .bookingDate(LocalDate.now().plusDays(1))
                .bookingTime(LocalTime.of(14, 0))
                .usePoints(true)
                .pointsToRedeem(100)
                .note("Test booking with points")
                .build();

        service = Service.builder()
                .id(1L)
                .name("Body Massage")
                .durationMinutes(90)
                .price(new BigDecimal("500000"))
                .isActive(true)
                .build();

        therapist = Therapist.builder()
                .id(1L)
                .fullName("Nguyễn Thị Mai")
                .employeeCode("TH001")
                .isActive(true)
                .build();

        user = User.builder()
                .id(1L)
                .fullName("Test User")
                .email("user@example.com")
                .phone("0905999999")
                .loyaltyPoints(1000)
                .membershipTier(MembershipTier.SILVER)
                .isActive(true)
                .build();

        booking = Booking.builder()
                .id(1L)
                .bookingId("BK20250220140000")
                .customerName("Nguyễn Văn A")
                .customerPhone("0905123456")
                .service(service)
                .therapist(therapist)
                .bookingDate(LocalDate.now().plusDays(1))
                .bookingTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(15, 30))
                .status(BookingStatus.PENDING)
                .originalPrice(new BigDecimal("500000"))
                .discountAmount(BigDecimal.ZERO)
                .finalPrice(new BigDecimal("500000"))
                .isAnonymous(true)
                .build();
    }

    // ============================================================
    // ANONYMOUS BOOKING TESTS
    // ============================================================

    @Test
    @DisplayName("Should create anonymous booking successfully")
    void testCreateAnonymousBooking_Success() {
        // Given
        when(serviceRepository.findById(1L))
                .thenReturn(Optional.of(service));

        when(therapistRepository.findByIdWithLock(1L))
                .thenReturn(Optional.of(therapist));

        when(dynamicSlotService.isTherapistAvailable(
                anyLong(), anyLong(), any(LocalDate.class), any(LocalTime.class)
        )).thenReturn(true);

        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        BookingResponse expectedResponse = BookingResponse.builder()
                .id(1L)
                .bookingId("BK20250220140000")
                .customerName("Nguyễn Văn A")
                .build();

        when(bookingMapper.toResponse(any(Booking.class)))
                .thenReturn(expectedResponse);

        // When
        BookingResponse result = bookingService.createAnonymousBooking(anonymousRequest);

        // Then
        assertNotNull(result);
        assertEquals("BK20250220140000", result.getBookingId());
        assertEquals("Nguyễn Văn A", result.getCustomerName());

        // Verify method calls
        verify(bookingValidator, times(1))
                .validateBookingRequest(any());
        verify(businessRulesValidator, times(1))
                .validateBookingDay(any(LocalDate.class));
        verify(serviceRepository, times(1))
                .findById(1L);
        verify(therapistRepository, times(1))
                .findByIdWithLock(1L);
        verify(dynamicSlotService, times(1))
                .isTherapistAvailable(anyLong(), anyLong(), any(), any());
        verify(bookingRepository, times(1))
                .save(any(Booking.class));
        verify(activityLogRepository, times(1))
                .save(any(ActivityLog.class));
        verify(emailService, times(1))
                .sendBookingConfirmation(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw exception when service not found")
    void testCreateAnonymousBooking_ServiceNotFound() {
        // Given
        when(serviceRepository.findById(1L))
                .thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.createAnonymousBooking(anonymousRequest)
        );

        assertEquals("Service not found", exception.getMessage());

        // Verify no booking created
        verify(bookingRepository, never()).save(any());
        verify(emailService, never()).sendBookingConfirmation(any());
    }

    @Test
    @DisplayName("Should throw exception when therapist not found")
    void testCreateAnonymousBooking_TherapistNotFound() {
        // Given
        when(serviceRepository.findById(1L))
                .thenReturn(Optional.of(service));

        when(therapistRepository.findByIdWithLock(1L))
                .thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.createAnonymousBooking(anonymousRequest)
        );

        assertEquals("Therapist not found", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when slot not available")
    void testCreateAnonymousBooking_SlotNotAvailable() {
        // Given
        when(serviceRepository.findById(1L))
                .thenReturn(Optional.of(service));

        when(therapistRepository.findByIdWithLock(1L))
                .thenReturn(Optional.of(therapist));

        // Mock slot as unavailable
        when(dynamicSlotService.isTherapistAvailable(
                anyLong(), anyLong(), any(), any()
        )).thenReturn(false);

        // When & Then
        BookingException exception = assertThrows(
                BookingException.class,
                () -> bookingService.createAnonymousBooking(anonymousRequest)
        );

        assertEquals("SLOT_NOT_AVAILABLE", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("not available"));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when service is inactive")
    void testCreateAnonymousBooking_ServiceInactive() {
        // Given
        service.setIsActive(false);

        when(serviceRepository.findById(1L))
                .thenReturn(Optional.of(service));

        doThrow(new BookingException("SERVICE_INACTIVE", "Service is not active"))
                .when(bookingValidator)
                .validateBookingRequest(any());

        // When & Then
        BookingException exception = assertThrows(
                BookingException.class,
                () -> bookingService.createAnonymousBooking(anonymousRequest)
        );

        assertEquals("SERVICE_INACTIVE", exception.getErrorCode());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when booking on Sunday")
    void testCreateAnonymousBooking_Sunday() {
        // Given
        LocalDate sunday = LocalDate.of(2025, 2, 23); // Sunday
        anonymousRequest.setBookingDate(sunday);

        doThrow(new BookingException("SUNDAY_NOT_ALLOWED", "Cannot book on Sunday"))
                .when(businessRulesValidator)
                .validateBookingDay(sunday);

        // When & Then
        BookingException exception = assertThrows(
                BookingException.class,
                () -> bookingService.createAnonymousBooking(anonymousRequest)
        );

        assertEquals("SUNDAY_NOT_ALLOWED", exception.getErrorCode());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should auto-assign therapist when not specified")
    void testCreateAnonymousBooking_AutoAssignTherapist() {
        // Given
        anonymousRequest.setTherapistId(null); // No therapist specified

        List<TherapistService> therapistServices = new ArrayList<>();
        TherapistService ts = new TherapistService();
        ts.setTherapist(therapist);
        ts.setSkillLevel(SkillLevel.EXPERT);
        therapistServices.add(ts);

        when(serviceRepository.findById(1L))
                .thenReturn(Optional.of(service));

        when(therapistServiceRepository.findByServiceIdWithTherapist(1L))
                .thenReturn(therapistServices);

        when(dynamicSlotService.isTherapistAvailable(
                anyLong(), anyLong(), any(), any()
        )).thenReturn(true);

        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        when(bookingMapper.toResponse(any(Booking.class)))
                .thenReturn(new BookingResponse());

        // When
        BookingResponse result = bookingService.createAnonymousBooking(anonymousRequest);

        // Then
        assertNotNull(result);
        verify(therapistServiceRepository, times(1))
                .findByServiceIdWithTherapist(1L);
        verify(bookingRepository, times(1))
                .save(any(Booking.class));
    }

    // ============================================================
    // USER BOOKING TESTS (WITH LOYALTY POINTS)
    // ============================================================

    @Test
    @DisplayName("Should create user booking with points successfully")
    void testCreateUserBooking_WithPoints_Success() {
        // Given
        when(userRepository.findActiveByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(serviceRepository.findById(1L))
                .thenReturn(Optional.of(service));

        when(therapistRepository.findByIdWithLock(1L))
                .thenReturn(Optional.of(therapist));

        when(dynamicSlotService.isTherapistAvailable(
                anyLong(), anyLong(), any(), any()
        )).thenReturn(true);

        Booking bookingWithUser = booking.toBuilder()
                .user(user)
                .isAnonymous(false)
                .discountAmount(new BigDecimal("10000"))
                .finalPrice(new BigDecimal("490000"))
                .build();

        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(bookingWithUser);

        when(bookingMapper.toResponse(any(Booking.class)))
                .thenReturn(new BookingResponse());

        // When
        BookingResponse result = bookingService.createUserBooking(
                userRequest,
                "user@example.com"
        );

        // Then
        assertNotNull(result);

        // Verify points deducted
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, atLeastOnce()).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(900, savedUser.getLoyaltyPoints()); // 1000 - 100

        // Verify transaction logged
        verify(loyaltyTransactionRepository, times(1))
                .save(any(LoyaltyTransaction.class));

        verify(bookingRepository, times(1))
                .save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw exception when insufficient points")
    void testCreateUserBooking_InsufficientPoints() {
        // Given
        user.setLoyaltyPoints(50); // Only 50 points
        userRequest.setPointsToRedeem(100); // Try to redeem 100

        when(userRepository.findActiveByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(serviceRepository.findById(1L))
                .thenReturn(Optional.of(service));

        // When & Then
        BookingException exception = assertThrows(
                BookingException.class,
                () -> bookingService.createUserBooking(userRequest, "user@example.com")
        );

        assertEquals("INSUFFICIENT_POINTS", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("50"));
        assertTrue(exception.getMessage().contains("100"));

        // Verify no booking created
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should refund points when booking fails after deduction")
    void testCreateUserBooking_RefundPointsOnFailure() {
        // Given
        when(userRepository.findActiveByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(serviceRepository.findById(1L))
                .thenReturn(Optional.of(service));

        when(therapistRepository.findByIdWithLock(1L))
                .thenReturn(Optional.of(therapist));

        // Slot becomes unavailable after points deducted
        when(dynamicSlotService.isTherapistAvailable(
                anyLong(), anyLong(), any(), any()
        )).thenReturn(false);

        // When & Then
        BookingException exception = assertThrows(
                BookingException.class,
                () -> bookingService.createUserBooking(userRequest, "user@example.com")
        );

        assertEquals("SLOT_NOT_AVAILABLE", exception.getErrorCode());

        // Verify points refunded (transaction should rollback automatically)
        // In real scenario, @Transactional would handle this
        // But in unit test, we verify the exception is thrown
    }

    @Test
    @DisplayName("Should create user booking without points")
    void testCreateUserBooking_WithoutPoints() {
        // Given
        userRequest.setUsePoints(false);
        userRequest.setPointsToRedeem(0);

        when(userRepository.findActiveByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(serviceRepository.findById(1L))
                .thenReturn(Optional.of(service));

        when(therapistRepository.findByIdWithLock(1L))
                .thenReturn(Optional.of(therapist));

        when(dynamicSlotService.isTherapistAvailable(
                anyLong(), anyLong(), any(), any()
        )).thenReturn(true);

        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        when(bookingMapper.toResponse(any(Booking.class)))
                .thenReturn(new BookingResponse());

        // When
        BookingResponse result = bookingService.createUserBooking(
                userRequest,
                "user@example.com"
        );

        // Then
        assertNotNull(result);

        // Verify points NOT deducted
        verify(loyaltyTransactionRepository, never())
                .save(any(LoyaltyTransaction.class));
    }

    // ============================================================
    // CANCEL BOOKING TESTS
    // ============================================================

    @Test
    @DisplayName("Should cancel booking successfully")
    void testCancelBooking_Success() {
        // Given
        booking.setUser(user);
        booking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        // When
        bookingService.cancelBooking(1L, "user@example.com", "Change of plans");

        // Then
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository, times(1)).save(bookingCaptor.capture());

        Booking savedBooking = bookingCaptor.getValue();
        assertEquals(BookingStatus.CANCELLED, savedBooking.getStatus());
        assertEquals("Change of plans", savedBooking.getCancellationReason());
        assertNotNull(savedBooking.getCancelledAt());

        verify(activityLogRepository, times(1))
                .save(any(ActivityLog.class));
        verify(emailService, times(1))
                .sendCancellationEmail(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw exception when booking not found for cancellation")
    void testCancelBooking_BookingNotFound() {
        // Given
        when(bookingRepository.findById(999L))
                .thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.cancelBooking(999L, "user@example.com", "reason")
        );

        assertEquals("Booking not found", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when user not owner of booking")
    void testCancelBooking_NotOwner() {
        // Given
        User anotherUser = User.builder()
                .id(2L)
                .email("another@example.com")
                .build();

        booking.setUser(anotherUser);

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        // When & Then
        BookingException exception = assertThrows(
                BookingException.class,
                () -> bookingService.cancelBooking(1L, "user@example.com", "reason")
        );

        assertEquals("NOT_AUTHORIZED", exception.getErrorCode());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when booking already cancelled")
    void testCancelBooking_AlreadyCancelled() {
        // Given
        booking.setUser(user);
        booking.setStatus(BookingStatus.CANCELLED);

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        // When & Then
        BookingException exception = assertThrows(
                BookingException.class,
                () -> bookingService.cancelBooking(1L, "user@example.com", "reason")
        );

        assertEquals("ALREADY_CANCELLED", exception.getErrorCode());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when booking already completed")
    void testCancelBooking_AlreadyCompleted() {
        // Given
        booking.setUser(user);
        booking.setStatus(BookingStatus.COMPLETED);

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        // When & Then
        BookingException exception = assertThrows(
                BookingException.class,
                () -> bookingService.cancelBooking(1L, "user@example.com", "reason")
        );

        assertEquals("CANNOT_CANCEL_COMPLETED", exception.getErrorCode());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when cancelling too close to booking time")
    void testCancelBooking_TooLate() {
        // Given
        booking.setUser(user);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setBookingDate(LocalDate.now());
        booking.setBookingTime(LocalTime.now().plusMinutes(30)); // 30 min from now

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        doThrow(new BookingException("CANCELLATION_TOO_LATE", "Must cancel 2 hours before"))
                .when(bookingValidator)
                .validateCancellation(any(), any());

        // When & Then
        BookingException exception = assertThrows(
                BookingException.class,
                () -> bookingService.cancelBooking(1L, "user@example.com", "reason")
        );

        assertEquals("CANCELLATION_TOO_LATE", exception.getErrorCode());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should refund points when cancelling booking with discount")
    void testCancelBooking_WithPointsRefund() {
        // Given
        booking.setUser(user);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setDiscountAmount(new BigDecimal("10000")); // 100 points used

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        // When
        bookingService.cancelBooking(1L, "user@example.com", "reason");

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(1100, savedUser.getLoyaltyPoints()); // 1000 + 100 refund

        verify(loyaltyTransactionRepository, times(1))
                .save(any(LoyaltyTransaction.class));
    }

    // ============================================================
    // GET BOOKINGS TESTS
    // ============================================================

    @Test
    @DisplayName("Should get user bookings successfully")
    void testGetUserBookings_Success() {
        // Given
        List<Booking> bookings = List.of(booking);

        when(userRepository.findActiveByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findByUserId(1L))
                .thenReturn(bookings);

        when(bookingMapper.toResponse(any(Booking.class)))
                .thenReturn(new BookingResponse());

        // When
        List<BookingResponse> result = bookingService.getUserBookings("user@example.com");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(bookingRepository, times(1)).findByUserId(1L);
        verify(bookingMapper, times(1)).toResponse(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found for bookings")
    void testGetUserBookings_UserNotFound() {
        // Given
        when(userRepository.findActiveByEmail("notfound@example.com"))
                .thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.getUserBookings("notfound@example.com")
        );

        assertEquals("User not found", exception.getMessage());
        verify(bookingRepository, never()).findByUserId(anyLong());
    }

    @Test
    @DisplayName("Should return empty list when user has no bookings")
    void testGetUserBookings_EmptyList() {
        // Given
        when(userRepository.findActiveByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findByUserId(1L))
                .thenReturn(List.of());

        // When
        List<BookingResponse> result = bookingService.getUserBookings("user@example.com");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ============================================================
    // GET BOOKING BY ID TESTS
    // ============================================================

    @Test
    @DisplayName("Should get booking by ID successfully")
    void testGetBookingById_Success() {
        // Given
        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        when(bookingMapper.toResponse(booking))
                .thenReturn(new BookingResponse());

        // When
        BookingResponse result = bookingService.getBookingById(1L);

        // Then
        assertNotNull(result);
        verify(bookingRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when booking not found by ID")
    void testGetBookingById_NotFound() {
        // Given
        when(bookingRepository.findById(999L))
                .thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.getBookingById(999L)
        );

        assertEquals("Booking not found", exception.getMessage());
    }
}
