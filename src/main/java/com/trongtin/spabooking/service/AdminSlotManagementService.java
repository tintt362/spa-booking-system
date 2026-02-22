package com.trongtin.spabooking.service;

import com.trongtin.spabooking.dto.request.*;
import com.trongtin.spabooking.dto.response.*;
import com.trongtin.spabooking.entity.*;
import com.trongtin.spabooking.exception.*;
import com.trongtin.spabooking.mapper.BookingMapper;
import com.trongtin.spabooking.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminSlotManagementService {

    private final BookingSlotRepository slotRepository;
    private final ServiceRepository serviceRepository;
    private final TherapistRepository therapistRepository;
    private final TherapistServiceRepository therapistServiceRepository;
    private final BookingMapper mapper;

    /**
     * Generate booking slots
     */
    @Transactional
    public SlotGenerationResult generateSlots(GenerateSlotsRequest request) {
        log.info("Generating slots: {} to {}", request.getStartDate(), request.getEndDate());

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BookingException(
                    "INVALID_DATE_RANGE",
                    "Ngày kết thúc phải sau ngày bắt đầu"
            );
        }

        // Get services
        List<com.trongtin.spabooking.entity.Service> services;
        if (request.getServiceIds() != null && !request.getServiceIds().isEmpty()) {
            services = serviceRepository.findAllById(request.getServiceIds());
        } else {
            services = serviceRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        }

        // Get therapists
        List<Therapist> therapists;
        if (request.getTherapistIds() != null && !request.getTherapistIds().isEmpty()) {
            therapists = therapistRepository.findAllById(request.getTherapistIds());
        } else {
            therapists = therapistRepository.findByIsActiveTrue();
        }

        // Default times
        LocalTime startTime = request.getStartTime() != null
                ? LocalTime.parse(request.getStartTime())
                : LocalTime.of(8, 0);

        LocalTime endTime = request.getEndTime() != null
                ? LocalTime.parse(request.getEndTime())
                : LocalTime.of(20, 0);

        int slotInterval = request.getSlotInterval() != null
                ? request.getSlotInterval()
                : 30;

        int totalGenerated = 0;
        int skipped = 0;
        int overwritten = 0;

        // Generate slots for each date
        LocalDate currentDate = request.getStartDate();
        while (!currentDate.isAfter(request.getEndDate())) {

            // Skip weekends if requested
            if (request.getSkipWeekends() &&
                    (currentDate.getDayOfWeek() == DayOfWeek.SATURDAY ||
                            currentDate.getDayOfWeek() == DayOfWeek.SUNDAY)) {
                currentDate = currentDate.plusDays(1);
                continue;
            }

            // Generate slots for this date
            for (com.trongtin.spabooking.entity.Service service : services) {
                for (Therapist therapist : therapists) {

                    // Check if therapist can do this service
                    boolean canDoService = therapistServiceRepository
                            .existsByTherapistIdAndServiceId(therapist.getId(), service.getId());

                    if (!canDoService) {
                        continue;
                    }

                    // Generate time slots
                    LocalTime currentTime = startTime;
                    while (currentTime.isBefore(endTime)) {
                        LocalTime slotEndTime = currentTime.plusMinutes(service.getDurationMinutes());

                        // Check if slot would end after closing time
                        if (slotEndTime.isAfter(endTime)) {
                            break;
                        }

                        // Check if slot already exists
                        Optional<BookingSlot> existing = slotRepository
                                .findAvailableSlotForUpdate(
                                        service.getId(),
                                        therapist.getId(),
                                        currentDate,
                                        currentTime
                                );

                        if (existing.isPresent()) {
                            if (request.getOverwriteExisting() && !existing.get().getIsBooked()) {
                                // Overwrite
                                slotRepository.delete(existing.get());
                                overwritten++;
                            } else {
                                // Skip
                                skipped++;
                                currentTime = currentTime.plusMinutes(slotInterval);
                                continue;
                            }
                        }

                        // Create new slot
                        BookingSlot slot = BookingSlot.builder()
                                .service(service)
                                .therapist(therapist)
                                .bookingDate(currentDate)
                                .bookingTime(currentTime)
                                .endTime(slotEndTime)
                                .isBooked(false)
                                .isBlocked(false)
                                .build();

                        slotRepository.save(slot);
                        totalGenerated++;

                        currentTime = currentTime.plusMinutes(slotInterval);
                    }
                }
            }

            currentDate = currentDate.plusDays(1);
        }

        log.info("Slot generation completed: generated={}, skipped={}, overwritten={}",
                totalGenerated, skipped, overwritten);

        return SlotGenerationResult.builder()
                .totalGenerated(totalGenerated)
                .skipped(skipped)
                .overwritten(overwritten)
                .message(String.format("Đã tạo %d slots mới", totalGenerated))
                .build();
    }

    /**
     * Get slots with filters
     */
    @Transactional(readOnly = true)
    public Page<SlotDTO> getSlots(
            LocalDate date,
            Long serviceId,
            Long therapistId,
            Boolean booked,
            Boolean blocked,
            Pageable pageable
    ) {
        List<BookingSlot> allSlots = slotRepository.findAll();

        // Apply filters
        List<BookingSlot> filtered = allSlots.stream()
                .filter(slot -> date == null || slot.getBookingDate().equals(date))
                .filter(slot -> serviceId == null || slot.getService().getId().equals(serviceId))
                .filter(slot -> therapistId == null ||
                        (slot.getTherapist() != null && slot.getTherapist().getId().equals(therapistId)))
                .filter(slot -> booked == null || slot.getIsBooked().equals(booked))
                .filter(slot -> blocked == null || slot.getIsBlocked().equals(blocked))
                .toList();

        // Convert to DTOs
        List<SlotDTO> slotDTOs = filtered.stream()
                .map(this::mapToSlotDTO)
                .collect(Collectors.toList());

        // Paginate
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), slotDTOs.size());

        List<SlotDTO> pageContent = slotDTOs.subList(start, end);

        return new PageImpl<>(pageContent, pageable, slotDTOs.size());
    }

    /**
     * Block multiple slots
     */
    @Transactional
    public int blockSlots(BlockSlotsRequest request) {
        log.info("Blocking slots for therapist {}: {} to {}",
                request.getTherapistId(), request.getStartDate(), request.getEndDate());

        Therapist therapist = therapistRepository.findById(request.getTherapistId())
                .orElseThrow(() -> new ResourceNotFoundException("Therapist"));

        List<BookingSlot> slots = slotRepository.findAll().stream()
                .filter(slot -> slot.getTherapist() != null)
                .filter(slot -> slot.getTherapist().getId().equals(request.getTherapistId()))
                .filter(slot -> !slot.getBookingDate().isBefore(request.getStartDate()))
                .filter(slot -> !slot.getBookingDate().isAfter(request.getEndDate()))
                .filter(slot -> !slot.getIsBooked())
                .filter(slot -> {
                    // Time filter
                    if (request.getStartTime() != null && request.getEndTime() != null) {
                        LocalTime start = LocalTime.parse(request.getStartTime());
                        LocalTime end = LocalTime.parse(request.getEndTime());
                        return !slot.getBookingTime().isBefore(start) &&
                                slot.getBookingTime().isBefore(end);
                    }
                    return true;
                })
                .filter(slot -> {
                    // Service filter
                    if (request.getServiceIds() != null && !request.getServiceIds().isEmpty()) {
                        return request.getServiceIds().contains(slot.getService().getId());
                    }
                    return true;
                })
                .toList();

        for (BookingSlot slot : slots) {
            slot.setIsBlocked(true);
            slot.setBlockReason(request.getReason());
        }

        slotRepository.saveAll(slots);

        log.info("Blocked {} slots", slots.size());

        return slots.size();
    }

    /**
     * Unblock a slot
     */
    @Transactional
    public void unblockSlot(Long id) {
        log.info("Unblocking slot: id={}", id);

        BookingSlot slot = slotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Slot"));

        if (slot.getIsBooked()) {
            throw new BookingException(
                    "SLOT_BOOKED",
                    "Không thể mở khóa slot đã được đặt"
            );
        }

        slot.setIsBlocked(false);
        slot.setBlockReason(null);
        slotRepository.save(slot);

        log.info("Slot unblocked: id={}", id);
    }

    /**
     * Clean up old unbooked slots
     */
    @Transactional
    public int cleanupOldSlots(LocalDate beforeDate) {
        log.info("Cleaning up slots before: {}", beforeDate);

        List<BookingSlot> oldSlots = slotRepository.findAll().stream()
                .filter(slot -> slot.getBookingDate().isBefore(beforeDate))
                .filter(slot -> !slot.getIsBooked())
                .toList();

        slotRepository.deleteAll(oldSlots);

        log.info("Deleted {} old slots", oldSlots.size());

        return oldSlots.size();
    }

    /**
     * Get slot statistics
     */
    @Transactional(readOnly = true)
    public SlotStatisticsDTO getSlotStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("Getting slot statistics: {} to {}", startDate, endDate);

        List<BookingSlot> slots = slotRepository.findAll().stream()
                .filter(slot -> !slot.getBookingDate().isBefore(startDate))
                .filter(slot -> !slot.getBookingDate().isAfter(endDate))
                .toList();

        long total = slots.size();
        long booked = slots.stream().filter(BookingSlot::getIsBooked).count();
        long blocked = slots.stream().filter(BookingSlot::getIsBlocked).count();
        long available = total - booked - blocked;

        double utilizationRate = total > 0 ? (booked * 100.0 / total) : 0.0;

        // Slots by service
        Map<String, Long> slotsByService = slots.stream()
                .collect(Collectors.groupingBy(
                        slot -> slot.getService().getName(),
                        Collectors.counting()
                ));

        // Slots by therapist
        Map<String, Long> slotsByTherapist = slots.stream()
                .filter(slot -> slot.getTherapist() != null)
                .collect(Collectors.groupingBy(
                        slot -> slot.getTherapist().getFullName(),
                        Collectors.counting()
                ));

        // Utilization by service
        Map<String, Double> utilizationByService = new HashMap<>();
        for (String serviceName : slotsByService.keySet()) {
            long serviceTotal = slotsByService.get(serviceName);
            long serviceBooked = slots.stream()
                    .filter(slot -> slot.getService().getName().equals(serviceName))
                    .filter(BookingSlot::getIsBooked)
                    .count();

            double rate = serviceTotal > 0 ? (serviceBooked * 100.0 / serviceTotal) : 0.0;
            utilizationByService.put(serviceName, rate);
        }

        return SlotStatisticsDTO.builder()
                .totalSlots(total)
                .bookedSlots(booked)
                .blockedSlots(blocked)
                .availableSlots(available)
                .utilizationRate(utilizationRate)
                .slotsByService(slotsByService)
                .slotsByTherapist(slotsByTherapist)
                .utilizationByService(utilizationByService)
                .build();
    }

    /**
     * Helper: Map slot to DTO
     */
    private SlotDTO mapToSlotDTO(BookingSlot slot) {
        return SlotDTO.builder()
                .id(slot.getId())
                .bookingDate(slot.getBookingDate())
                .bookingTime(slot.getBookingTime())
                .endTime(slot.getEndTime())
                .service(mapper.toServiceDTO(slot.getService()))
                .therapist(slot.getTherapist() != null
                        ? mapper.toTherapistDTO(slot.getTherapist())
                        : null)
                .isBooked(slot.getIsBooked())
                .isBlocked(slot.getIsBlocked())
                .blockReason(slot.getBlockReason())
                .bookingId(slot.getBookingId())
                .build();
    }
}