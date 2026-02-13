package com.trongtin.spabooking.repository;

import com.trongtin.spabooking.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    // kiem tat ca service active - order asc
    List<Service> findByIsActiveTrueOrderByDisplayOrderAsc();
    Optional<Service> findBySlug(String slug);

    // lat tat ca service active va category truyen vo
    @Query("SELECT s FROM Service s  WHERE s.isActive = true and s.category = :category")
    List<Service> findActiveByCategory(String category);
}
