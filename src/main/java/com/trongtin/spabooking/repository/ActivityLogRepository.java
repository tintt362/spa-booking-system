package com.trongtin.spabooking.repository;

import com.trongtin.spabooking.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findByBookingIdOrderByCreatedAtDesc(Long bookingId);

}
