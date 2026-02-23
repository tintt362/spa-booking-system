package com.trongtin.spabooking.service;


import com.trongtin.spabooking.entity.*;
import com.trongtin.spabooking.entity.Service;
import com.trongtin.spabooking.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class SlotGenerationService {

    private final BookingSlotRepository slotRepository;
    private final ServiceRepository serviceRepository;
    private final TherapistRepository therapistRepository;

    /**
     * Generate slots for next N days
     */
    @Transactional
    public int generateSlotsForNextDays(int days) {
        log.info("Generating slots for next {} days", days);

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);

        List<Service> services =
                serviceRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        List<Therapist> therapists =
                therapistRepository.findByIsActiveTrue();

        int totalSlots = 0;

        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
            // Skip Sundays
            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }

            int dailySlots = generateSlotsForDate(date, services, therapists);
            totalSlots += dailySlots;
        }

        log.info("Generated {} slots total", totalSlots);
        return totalSlots;
    }

    /**
     * Generate slots for a specific date
     */
    @Transactional
    public int generateSlotsForDate(
            LocalDate date,
            List<Service> services,
            List<Therapist> therapists
    ) {
        List<BookingSlot> slots = new ArrayList<>();

        for (Service service : services) {
            for (Therapist therapist : therapists) {
                // Check if therapist can do this service
                boolean canDoService = therapist.getTherapistServices().stream()
                        .anyMatch(ts -> ts.getService().getId().equals(service.getId()));

                if (!canDoService) {
                    continue;
                }

                // Generate slots from 8:00 to 20:00, every 30 minutes
                LocalTime currentTime = LocalTime.of(8, 0);
                LocalTime closeTime = LocalTime.of(20, 0);

                while (currentTime.isBefore(closeTime)) {
                    LocalTime endTime = currentTime.plusMinutes(service.getDurationMinutes());

                    // Check if service would end after closing time
                    if (endTime.isAfter(closeTime)) {
                        break;
                    }

                    // Check if slot already exists
                    boolean exists = slotRepository
                            .findAvailableSlotForUpdate(
                                    service.getId(),
                                    therapist.getId(),
                                    date,
                                    currentTime
                            )
                            .isPresent();

                    if (!exists) {
                        BookingSlot slot = BookingSlot.builder()
                                .service(service)
                                .therapist(therapist)
                                .bookingDate(date)
                                .bookingTime(currentTime)
                                .endTime(endTime)
                                .isBooked(false)
                                .isBlocked(false)
                                .build();

                        slots.add(slot);
                    }

                    // Next slot (30 minutes interval)
                    currentTime = currentTime.plusMinutes(30);
                }
            }
        }

        if (!slots.isEmpty()) {
            slotRepository.saveAll(slots);
            log.info("Generated {} slots for date: {}", slots.size(), date);
        }

        return slots.size();
    }

    /**
     * Block slots for a therapist on specific date/time range
     */
    @Transactional
    public int blockSlotsForTherapist(
            Long therapistId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            String reason
    ) {
        log.info("Blocking slots for therapist {} on {} from {} to {}",
                therapistId, date, startTime, endTime);

        List<BookingSlot> slots = slotRepository.findAll().stream()
                .filter(slot -> slot.getTherapist() != null)
                .filter(slot -> slot.getTherapist().getId().equals(therapistId))
                .filter(slot -> slot.getBookingDate().equals(date))
                .filter(slot -> !slot.getBookingTime().isBefore(startTime))
                .filter(slot -> slot.getBookingTime().isBefore(endTime))
                .filter(slot -> !slot.getIsBooked())
                .toList();
        log.info("SlotGenerationService {} slots", slots);

        for (BookingSlot slot : slots) {
            slot.setIsBlocked(true);
            slot.setBlockReason(reason);
        }

        slotRepository.saveAll(slots);

        log.info("Blocked {} slots", slots.size());
        return slots.size();
    }

    /**
     * Unblock slots
     */
    @Transactional
    public int unblockSlots(List<Long> slotIds) {
        List<BookingSlot> slots = slotRepository.findAllById(slotIds);

        for (BookingSlot slot : slots) {
            if (!slot.getIsBooked()) {
                slot.setIsBlocked(false);
                slot.setBlockReason(null);
            }
        }

        slotRepository.saveAll(slots);

        log.info("Unblocked {} slots", slots.size());
        return slots.size();
    }
}