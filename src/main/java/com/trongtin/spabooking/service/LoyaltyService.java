package com.trongtin.spabooking.service;

import com.trongtin.spabooking.dto.response.LoyaltyTransactionDTO;
import com.trongtin.spabooking.entity.*;
import com.trongtin.spabooking.exception.*;
import com.trongtin.spabooking.repository.LoyaltyTransactionRepository;
import com.trongtin.spabooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyService {

    private final UserRepository userRepository;
    private final LoyaltyTransactionRepository transactionRepository;

    private static final BigDecimal POINTS_PER_100K = new BigDecimal("10"); // 10 points per 100k VND

    ///     Calculate and award points for booking
    @Transactional
    public void awardPointsForBooking(Long userId, Long bookingId, BigDecimal amount) {
        log.info("Awarding points for booking: userId={}, amount={}", userId, amount);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User"));

        // Calculate points (10 points per 100k)
        int points = amount
                .divide(new BigDecimal("100000"), 0, java.math.RoundingMode.DOWN)
                .multiply(POINTS_PER_100K)
                .intValue();

        if (points > 0) {
            addPoints(
                    user,
                    bookingId,
                    points,
                    TransactionType.EARN_BOOKING,
                    "Tích điểm từ booking"
            );

            // Update membership tier
            updateMembershipTier(user);
        }
    }

    ///       Redeem points (deduct)

    @Transactional
    public void redeemPoints(Long userId, Long bookingId, int points) {
        log.info("Redeeming points: userId={}, points={}", userId, points);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User"));

        if (user.getLoyaltyPoints() < points) {
            throw new IllegalArgumentException("Insufficient points");
        }

        addPoints(
                user,
                bookingId,
                -points, // Negative for redemption
                TransactionType.REDEEM_DISCOUNT,
                "Đổi điểm giảm giá"
        );
    }

    ///     Add points (positive or negative)
    @Transactional
    public void addPoints(
            User user,
            Long bookingId,
            int points,
            TransactionType type,
            String description
    ) {
        int balanceBefore = user.getLoyaltyPoints();
        int balanceAfter = balanceBefore + points;

        // Update user balance
        user.setLoyaltyPoints(balanceAfter);
        userRepository.save(user);

        // Create transaction record
        LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                .user(user)
                .points(points)
                .transactionType(type)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .description(description)
                .build();

        // Set booking reference if provided
        if (bookingId != null) {
            Booking booking =
                    new Booking();
            booking.setId(bookingId);
            transaction.setBooking(booking);
        }

        transactionRepository.save(transaction);

        log.info("Points updated: userId={}, points={}, balance={}",
                user.getId(), points, balanceAfter);
    }

    ///     Update membership tier based on points
    @Transactional
    public void updateMembershipTier(User user) {
        int points = user.getLoyaltyPoints();
        MembershipTier currentTier = user.getMembershipTier();
        MembershipTier newTier;

        if (points >= 5000) {
            newTier = MembershipTier.PLATINUM;
        } else if (points >= 2000) {
            newTier = MembershipTier.GOLD;
        } else if (points >= 500) {
            newTier = MembershipTier.SILVER;
        } else {
            newTier = MembershipTier.BRONZE;
        }

        if (newTier != currentTier) {
            user.setMembershipTier(newTier);
            userRepository.save(user);

            log.info("Membership tier updated: userId={}, {} -> {}",
                    user.getId(), currentTier, newTier);
        }
    }

    ////     Get user's loyalty transactions
    @Transactional(readOnly = true)
    public Page<LoyaltyTransactionDTO> getTransactions(String email, Pageable pageable) {
        User user = userRepository.findActiveByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User"));

        Page<LoyaltyTransaction> transactions =
                transactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);

        return transactions.map(this::mapToDTO);
    }

    private LoyaltyTransactionDTO mapToDTO(LoyaltyTransaction transaction) {
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
