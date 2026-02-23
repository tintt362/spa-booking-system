package com.trongtin.spabooking.service;


import com.trongtin.spabooking.dto.request.*;
import com.trongtin.spabooking.dto.response.*;
import com.trongtin.spabooking.entity.*;
import com.trongtin.spabooking.exception.*;
import com.trongtin.spabooking.mapper.BookingMapper;
import com.trongtin.spabooking.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceManagementService {

    private final ServiceRepository serviceRepository;
    private final BookingMapper mapper;


     // Get all services
    @Transactional(readOnly = true)
    public List<ServiceDTO> getAllServices(boolean includeInactive) {
        List<Service> services;

        if (includeInactive) {
            services = serviceRepository.findAll();
        } else {
            services = serviceRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        }

        return services.stream()
                .map(mapper::toServiceDTO)
                .collect(Collectors.toList());
    }


     //Create new service
    @Transactional
    public ServiceDTO createService(CreateServiceRequest request) {
        log.info("Creating new service: {}", request.getName());

        // Check slug uniqueness
        if (serviceRepository.findBySlug(request.getSlug()).isPresent()) {
            throw new BookingException(
                    "SLUG_EXISTS",
                    "Slug đã tồn tại"
            );
        }

        // Validate discount price
        if (request.getDiscountPrice() != null &&
                request.getDiscountPrice().compareTo(request.getPrice()) >= 0) {
            throw new BookingException(
                    "INVALID_DISCOUNT",
                    "Giá khuyến mãi phải nhỏ hơn giá gốc"
            );
        }

        Service service = Service.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .durationMinutes(request.getDurationMinutes())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .imageUrl(request.getImageUrl())
                .category(request.getCategory())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(true)
                .build();

        Service saved = serviceRepository.save(service);

        log.info("Service created: id={}, name={}", saved.getId(), saved.getName());

        return mapper.toServiceDTO(saved);
    }


     // Update service
    @Transactional
    public ServiceDTO updateService(Long id, UpdateServiceRequest request) {
        log.info("Updating service: id={}", id);

        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service"));

        // Update fields
        if (request.getName() != null) {
            service.setName(request.getName());
        }

        if (request.getDescription() != null) {
            service.setDescription(request.getDescription());
        }

        if (request.getDurationMinutes() != null) {
            service.setDurationMinutes(request.getDurationMinutes());
        }

        if (request.getPrice() != null) {
            service.setPrice(request.getPrice());
        }

        if (request.getDiscountPrice() != null) {
            // Validate
            if (request.getDiscountPrice().compareTo(service.getPrice()) >= 0) {
                throw new BookingException(
                        "INVALID_DISCOUNT",
                        "Giá khuyến mãi phải nhỏ hơn giá gốc"
                );
            }
            service.setDiscountPrice(request.getDiscountPrice());
        }

        if (request.getImageUrl() != null) {
            service.setImageUrl(request.getImageUrl());
        }

        if (request.getCategory() != null) {
            service.setCategory(request.getCategory());
        }

        if (request.getDisplayOrder() != null) {
            service.setDisplayOrder(request.getDisplayOrder());
        }

        Service updated = serviceRepository.save(service);

        log.info("Service updated: id={}", id);

        return mapper.toServiceDTO(updated);
    }

    ///Soft delete service
    @Transactional
    public void deleteService(Long id) {
        log.info("Deleting service: id={}", id);

        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service"));

        // Soft delete
        service.setIsActive(false);
        serviceRepository.save(service);

        log.info("Service deleted (soft): id={}", id);
    }

    ///Activate service
    @Transactional
    public void activateService(Long id) {
        log.info("Activating service: id={}", id);

        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service"));

        service.setIsActive(true);
        serviceRepository.save(service);

        log.info("Service activated: id={}", id);
    }
}