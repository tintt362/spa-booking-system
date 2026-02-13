package com.trongtin.spabooking.repository;

import com.trongtin.spabooking.entity.TherapistService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TherapistServiceRepository extends JpaRepository<TherapistService, Long> {

    // kiem tat ca TherapistService bang ID cua Therapist
    @Query("SELECT ts FROM TherapistService ts JOIN FETCH ts.service s " +
            "WHERE ts.therapist.id = :therapistId AND s.isActive = true " +
            "ORDER BY ts.isPrimaryService DESC, ts.skillLevel DESC")
    List<TherapistService> findByTherapistIdWithService(@Param("therapistId") Long therapistId);

    // kiem tat ca TherapistService bang ID cua Service
    @Query("""
        SELECT ts FROM TherapistService ts
        JOIN FETCH ts.therapist t
        WHERE ts.service.id = :serviceId
        AND t.isActive = true
        ORDER BY ts.skillLevel DESC, ts.yearsExperience DESC
        """)
    List<TherapistService> findByServiceIdWithTherapist(@Param("serviceId") Long serviceId);

    boolean existsByTherapistIdAndServiceId(Long therapistId, Long serviceId);

}
