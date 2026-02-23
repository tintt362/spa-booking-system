package com.trongtin.spabooking.controller;

import com.trongtin.spabooking.dto.request.BlockSlotsRequest;
import com.trongtin.spabooking.dto.request.*;
import com.trongtin.spabooking.dto.response.ApiResponse;
import com.trongtin.spabooking.dto.response.*;
import com.trongtin.spabooking.service.AdminSlotManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/slots")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminSlotController {

    private final AdminSlotManagementService slotManagementService;


    //POST /api/admin/slots/generate
    //  Generate booking slots
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<SlotGenerationResult>> generateSlots(
            @Valid @RequestBody GenerateSlotsRequest request
    ) {
        SlotGenerationResult result = slotManagementService.generateSlots(request);

        return ResponseEntity.ok(
                ApiResponse.success("Tạo slots thành công!", result)
        );
    }

    /**
     * GET /api/admin/slots
     * Get slots with filters
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SlotDTO>>> getSlots(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) Long therapistId,
            @RequestParam(required = false) Boolean booked,
            @RequestParam(required = false) Boolean blocked,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<SlotDTO> slots = slotManagementService.getSlots(
                date, serviceId, therapistId, booked, blocked, pageable
        );

        return ResponseEntity.ok(ApiResponse.success(slots));
    }

    /**
     * POST /api/admin/slots/block
     * Block multiple slots
     */
    @PostMapping("/block")
    public ResponseEntity<ApiResponse<String>> blockSlots(
            @Valid @RequestBody BlockSlotsRequest request
    ) {
        int blockedCount = slotManagementService.blockSlots(request);

        return ResponseEntity.ok(
                ApiResponse.success("Đã khóa " + blockedCount + " slots")
        );
    }

    /**
     * PUT /api/admin/slots/{id}/unblock
     * Unblock a slot
     */
    @PutMapping("/{id}/unblock")
    public ResponseEntity<ApiResponse<String>> unblockSlot(@PathVariable Long id) {
        slotManagementService.unblockSlot(id);

        return ResponseEntity.ok(
                ApiResponse.success("Đã mở khóa slot")
        );
    }

    /**
     * DELETE /api/admin/slots/cleanup
     * Clean up old unbooked slots
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<ApiResponse<String>> cleanupOldSlots(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate beforeDate
    ) {
        if (beforeDate == null) {
            beforeDate = LocalDate.now().minusDays(90);
        }

        int deletedCount = slotManagementService.cleanupOldSlots(beforeDate);

        return ResponseEntity.ok(
                ApiResponse.success("Đã xóa " + deletedCount + " slots cũ")
        );
    }

    /**
     * GET /api/admin/slots/statistics
     * Get slot statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<SlotStatisticsDTO>> getStatistics(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        SlotStatisticsDTO stats = slotManagementService.getSlotStatistics(
                startDate, endDate
        );

        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}