package com.trongtin.spabooking.controller;

import com.trongtin.spabooking.dto.response.ApiResponse;
import com.trongtin.spabooking.dto.response.AvailableSlotDTO;
import com.trongtin.spabooking.dto.response.ServiceDTO;
import com.trongtin.spabooking.entity.Service;
import com.trongtin.spabooking.exception.ResourceNotFoundException;
import com.trongtin.spabooking.mapper.BookingMapper;
import com.trongtin.spabooking.repository.*;
import com.trongtin.spabooking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceRepository serviceRepository;
    private final BookingService bookingService;
    private final BookingMapper mapper;

    /**
     * GET /api/services
     * Get all active services
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceDTO>>> getAllServices() {
        List<Service> services = serviceRepository.findByIsActiveTrueOrderByDisplayOrderAsc();

        List<ServiceDTO> dtos = services.stream()
                .map(mapper::toServiceDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    /**
     * GET /api/services/{id}
     * Get service details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceDTO>> getService(@PathVariable Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service"));

        return ResponseEntity.ok(ApiResponse.success(mapper.toServiceDTO(service)));
    }

    /**
     * GET /api/services/{id}/available-slots
     * Get available time slots
     */
    @GetMapping("/{id}/available-slots")
    public ResponseEntity<ApiResponse<List<AvailableSlotDTO>>> getAvailableSlots(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<AvailableSlotDTO> slots = bookingService.getAvailableSlots(id, date);

        return ResponseEntity.ok(ApiResponse.success(slots));
    }
}