package com.trongtin.spabooking.repository;

import com.trongtin.spabooking.entity.Therapist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

    @Repository
    public interface TherapistRepository extends JpaRepository<Therapist, Long> {

        // kiem tat ca Therapist active = true
        List<Therapist> findByIsActiveTrue();

        // kiem tat ca Employee voi code
        Optional<Therapist> findByEmployeeCode(String code);

        // kiem tat ca Therapist bang email
        Optional<Therapist> findByEmail(String email);
    }
