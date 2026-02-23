package com.trongtin.spabooking.service;

import com.trongtin.spabooking.dto.request.*;
import com.trongtin.spabooking.dto.response.*;
import com.trongtin.spabooking.entity.*;
import com.trongtin.spabooking.exception.*;
import com.trongtin.spabooking.mapper.BookingMapper;
import com.trongtin.spabooking.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class AdminTherapistManagementService {

    private final TherapistRepository therapistRepository;
    private final ServiceRepository serviceRepository;
    private final TherapistServiceRepository therapistServiceRepository;
    private final BookingMapper mapper;

    /**
     * Get all therapists
     */
    @Transactional(readOnly = true)
    public List<TherapistDTO> getAllTherapists(boolean includeInactive) {
        List<Therapist> therapists;

        if (includeInactive) {
            therapists = therapistRepository.findAll();
        } else {
            therapists = therapistRepository.findByIsActiveTrue();
        }

        return therapists.stream()
                .map(mapper::toTherapistDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create new therapist
     */
    @Transactional
    public TherapistDTO createTherapist(CreateTherapistRequest request) {
        log.info("Creating new therapist: {}", request.getFullName());

        // Check employee code uniqueness
        if (therapistRepository.findByEmployeeCode(request.getEmployeeCode()).isPresent()) {
            throw new BookingException(
                    "EMPLOYEE_CODE_EXISTS",
                    "Mã nhân viên đã tồn tại"
            );
        }

        // Check email uniqueness
        if (request.getEmail() != null &&
                therapistRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BookingException(
                    "EMAIL_EXISTS",
                    "Email đã được sử dụng"
            );
        }

        Therapist therapist = Therapist.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .employeeCode(request.getEmployeeCode())
                .avatarUrl(request.getAvatarUrl())
                .isActive(true)
                .build();

        Therapist saved = therapistRepository.save(therapist);

        log.info("Therapist created: id={}, name={}", saved.getId(), saved.getFullName());

        return mapper.toTherapistDTO(saved);
    }

    /**
     * Update therapist
     */
    @Transactional
    public TherapistDTO updateTherapist(Long id, UpdateTherapistRequest request) {
        log.info("Updating therapist: id={}", id);

        Therapist therapist = therapistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Therapist"));

        if (request.getFullName() != null) {
            therapist.setFullName(request.getFullName());
        }

        if (request.getPhone() != null) {
            therapist.setPhone(request.getPhone());
        }

        if (request.getEmail() != null) {
            // Check uniqueness
            therapistRepository.findByEmail(request.getEmail())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new BookingException("EMAIL_EXISTS", "Email đã được sử dụng");
                        }
                    });

            therapist.setEmail(request.getEmail());
        }

        if (request.getAvatarUrl() != null) {
            therapist.setAvatarUrl(request.getAvatarUrl());
        }

        Therapist updated = therapistRepository.save(therapist);

        log.info("Therapist updated: id={}", id);

        return mapper.toTherapistDTO(updated);
    }

    /**
     * Deactivate therapist
     */
    @Transactional
    public void deactivateTherapist(Long id) {
        log.info("Deactivating therapist: id={}", id);

        Therapist therapist = therapistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Therapist"));

        therapist.setIsActive(false);
        therapistRepository.save(therapist);

        log.info("Therapist deactivated: id={}", id);
    }

    /**
     * Assign service to therapist
     */
    @Transactional
    public void assignService(Long therapistId, AssignServiceToTherapistRequest request) {
        log.info("Assigning service {} to therapist {}",
                request.getServiceId(), therapistId);

        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new ResourceNotFoundException("Therapist"));

        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service"));

        // Check if already assigned
        if (therapistServiceRepository.existsByTherapistIdAndServiceId(
                therapistId, request.getServiceId())) {
            throw new BookingException(
                    "ALREADY_ASSIGNED",
                    "Therapist đã được gán dịch vụ này"
            );
        }

        TherapistService ts = TherapistService.builder()
                .therapist(therapist)
                .service(service)
                .skillLevel(SkillLevel.valueOf(request.getSkillLevel()))
                .yearsExperience(request.getYearsExperience())
                .isPrimaryService(request.getIsPrimaryService())
                .build();

        therapistServiceRepository.save(ts);

        log.info("Service assigned to therapist successfully");
    }

    /**
     * Remove service from therapist
     */
    @Transactional
    public void removeService(Long therapistId, Long serviceId) {
        log.info("Removing service {} from therapist {}", serviceId, therapistId);

        List<TherapistService> assignments = therapistServiceRepository.findAll().stream()
                .filter(ts -> ts.getTherapist().getId().equals(therapistId))
                .filter(ts -> ts.getService().getId().equals(serviceId))
                .toList();

        if (assignments.isEmpty()) {
            throw new ResourceNotFoundException("Assignment not found");
        }

        therapistServiceRepository.deleteAll(assignments);

        log.info("Service removed from therapist successfully");
    }
}
