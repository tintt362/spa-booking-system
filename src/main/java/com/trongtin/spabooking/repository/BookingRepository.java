package com.trongtin.spabooking.repository;

import com.trongtin.spabooking.entity.Booking;
import com.trongtin.spabooking.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    //kiem ma booking id ma hien thi cho user
    Optional<Booking> findByBookingId(String bookingId);

    //kiem Booking bang Id (PK)
    @Query("""
            SELECT b FROM Booking b
                          LEFT JOIN FETCH b.service
                          LEFT JOIN FETCH b.therapist
                          LEFT JOIN FETCH b.user
                          WHERE b.id = :id
            """)
    Optional<Booking> findByIdWithDetails(@Param("id") Long id);

    // kiem Booking voi dieu kien loc
    @Query("""
            SELECT b FROM Booking b
            WHERE (:status IS NULL OR b.status = :status)
            AND (:date IS NULL OR b.bookingDate = :date)
            AND (:phone IS NULL OR b.customerPhone LIKE %:phone%)
            AND (:serviceId IS NULL OR b.service.id = :serviceId)
            ORDER BY b.createdAt DESC
            """)
    Page<Booking> findWithFilters(
            @Param("status") BookingStatus status,
            @Param("date") LocalDate date,
            @Param("phone") String phone,
            @Param("serviceId") Long serviceId,
            Pageable pageable
    );

    // kiem Booking bang User ID va sap xep theo ngay tao giam dan
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    // kiem Booking bang SDT va sap xep theo ngay tao giam dan
    List<Booking> findByCustomerPhoneOrderByCreatedAtDesc(String phone);


    // kiem tat ca Booking cua ngay hom nay
    @Query("""
            SELECT b FROM Booking b 
            WHERE b.bookingDate = :date
            AND b.status IN ('PENDING', 'CONFIRMED')
            ORDER BY b.bookingTime
            """)
    List<Booking> findTodayBookings(@Param("date") LocalDate date);

    // dem so luong booking theo status
    long countByStatus(BookingStatus status);


    // dem so luong booking bang khoang thoi gian
    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.bookingDate BETWEEN :startDate AND :endDate
        """)
    long countByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
