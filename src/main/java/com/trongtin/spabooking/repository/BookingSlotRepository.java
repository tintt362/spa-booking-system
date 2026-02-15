package com.trongtin.spabooking.repository;

import com.trongtin.spabooking.entity.BookingSlot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface BookingSlotRepository extends JpaRepository<BookingSlot, Long> {

    // Pessimistic Lock cho booking
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT s FROM BookingSlot s 
            WHERE s.service.id = :serviceId
            AND (:therapistId IS NULL OR s.therapistId = :therapistId)
            AND s.bookingDate = :bookingDate
            AND s.bookingTime = :bookingTime
            AND s.isBooked = false
            AND s.isBlocked = false
            """)
    // kiem slot trong de update(dat)
    Optional<BookingSlot> findAvailableSlotForUpdate(
            @Param("serviceId") Long serviceId,
            @Param("therapistId") Long therapistId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("bookingTime")LocalTime bookingTime
            );

    // lay slot con trong , hien thi dang dropdown
    @Query("""
        SELECT s FROM BookingSlot s
        JOIN FETCH s.service
        LEFT JOIN FETCH s.therapist
        WHERE s.service.id = :serviceId
        AND s.bookingDate = :bookingDate
        AND s.isBooked = false
        AND s.isBlocked = false
        ORDER BY s.bookingTime
        """)
    List<BookingSlot> findAvailableSlots(
            @Param("serviceId") Long serviceId,
            @Param("bookingDate") LocalDate bookingDate
    );

    // kiem BookingSlot theo BookingId
    Optional<BookingSlot> findByBookingId(Long bookingId);
}
