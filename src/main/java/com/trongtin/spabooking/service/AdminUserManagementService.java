package com.trongtin.spabooking.service;

import com.trongtin.spabooking.dto.response.*;
import com.trongtin.spabooking.dto.request.*;
import com.trongtin.spabooking.entity.*;
import com.trongtin.spabooking.entity.User;
import com.trongtin.spabooking.exception.*;
import com.trongtin.spabooking.mapper.BookingMapper;
import com.trongtin.spabooking.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserManagementService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final LoyaltyService loyaltyService;
    private final BookingMapper mapper;


      //Get users with filters
    @Transactional(readOnly = true)
    public Page<UserDTO> getUsers(
            String search,
            String tier,
            Boolean verified,
            Pageable pageable
    ) {
        Page<User> users = userRepository.findAll(pageable);

        // Apply filters (in real app, use Specification)
        if (search != null && !search.isEmpty()) {
            users = users.map(user -> {
                if (user.getFullName().toLowerCase().contains(search.toLowerCase()) ||
                        user.getEmail().toLowerCase().contains(search.toLowerCase()) ||
                        user.getPhone().contains(search)) {
                    return user;
                }
                return null;
            });
        }

        if (tier != null) {
            MembershipTier tierEnum = MembershipTier.valueOf(tier);
            users = users.map(user ->
                    user.getMembershipTier() == tierEnum ? user : null
            );
        }

        if (verified != null) {
            users = users.map(user ->
                    user.getIsVerified().equals(verified) ? user : null
            );
        }

        return users.map(mapper::toUserDTO);
    }

    /**
     * Get user detail
     */
    @Transactional(readOnly = true)
    public UserDetailDTO getUserDetail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User"));

        // Get customer info
        List<Booking> bookings =
                bookingRepository.findByUserIdOrderByCreatedAtDesc(id);

        CustomerInfoDTO customerInfo = calculateCustomerInfo(bookings, user);

        // Get recent bookings (last 5)
        List<BookingResponse> recentBookings = bookings.stream()
                .limit(5)
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        // Get recent transactions (last 10)
        List<LoyaltyTransaction> transactions =
                loyaltyTransactionRepository
                        .findByUserIdOrderByCreatedAtDesc(id,
                                org.springframework.data.domain.PageRequest.of(0, 10))
                        .getContent();

        List<LoyaltyTransactionDTO> recentTransactions = transactions.stream()
                .map(this::mapTransactionToDTO)
                .collect(Collectors.toList());

        return UserDetailDTO.builder()
                .user(mapper.toUserDTO(user))
                .customerInfo(customerInfo)
                .recentBookings(recentBookings)
                .recentTransactions(recentTransactions)
                .build();
    }

    /**
     * Verify user manually
     */
    @Transactional
    public void verifyUser(Long id) {
        log.info("Admin verifying user: id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User"));

        user.setIsVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("User verified: id={}", id);
    }

    /**
     * Deactivate user
     */
    @Transactional
    public void deactivateUser(Long id) {
        log.info("Deactivating user: id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User"));

        user.setIsActive(false);
        userRepository.save(user);

        log.info("User deactivated: id={}", id);
    }

    /**
     * Activate user
     */
    @Transactional
    public void activateUser(Long id) {
        log.info("Activating user: id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User"));

        user.setIsActive(true);
        userRepository.save(user);

        log.info("User activated: id={}", id);
    }

    /**
     * Adjust loyalty points
     */
    @Transactional
    public void adjustPoints(Long id, AdjustPointsRequest request) {
        log.info("Adjusting points for user {}: {}", id, request.getPoints());

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User"));

        loyaltyService.addPoints(
                user,
                null,
                request.getPoints(),
                TransactionType.ADJUSTMENT,
                "Admin điều chỉnh: " + request.getReason()
        );

        log.info("Points adjusted for user: id={}", id);
    }


     //Helper: Calculate customer info
    private CustomerInfoDTO calculateCustomerInfo(
            List<Booking> bookings,
            User user
    ) {
        long completed = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .count();

        long cancelled = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELLED)
                .count();

        java.math.BigDecimal totalSpent = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .map(Booking::getFinalPrice)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        return CustomerInfoDTO.builder()
                .totalBookings((long) bookings.size())
                .completedBookings(completed)
                .cancelledBookings(cancelled)
                .totalSpent(totalSpent)
                .loyaltyPoints(user.getLoyaltyPoints())
                .membershipTier(user.getMembershipTier().name())
                .build();
    }


     //Helper: Map transaction to DTO

    private LoyaltyTransactionDTO mapTransactionToDTO(
            LoyaltyTransaction transaction
    ) {
        return LoyaltyTransactionDTO.builder()
                .id(transaction.getId())
                .points(transaction.getPoints())
                .transactionType(transaction.getTransactionType().name())
                .description(transaction.getDescription())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}