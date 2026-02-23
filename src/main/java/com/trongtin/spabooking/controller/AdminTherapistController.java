package com.trongtin.spabooking.controller;

import com.trongtin.spabooking.dto.request.*    ;
import com.trongtin.spabooking.dto.response.ApiResponse;
import com.trongtin.spabooking.dto.response.TherapistDTO;
import com.trongtin.spabooking.service.AdminTherapistManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/therapists")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminTherapistController {

    private final AdminTherapistManagementService therapistManagementService;

    /**
     * GET /api/admin/therapists
     * Get all therapists
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TherapistDTO>>> getAllTherapists(
            @RequestParam(required = false) Boolean includeInactive
    ) {
        List<TherapistDTO> therapists = therapistManagementService.getAllTherapists(
                includeInactive != null && includeInactive
        );

        return ResponseEntity.ok(ApiResponse.success(therapists));
    }


    //POST /api/admin/therapists
    // Create new therapist

    @PostMapping
    public ResponseEntity<ApiResponse<TherapistDTO>> createTherapist(
            @Valid @RequestBody CreateTherapistRequest request
    ) {
        TherapistDTO therapist = therapistManagementService.createTherapist(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo therapist thành công!", therapist));
    }

     // PUT /api/admin/therapists/{id}
     //Update therapist

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TherapistDTO>> updateTherapist(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTherapistRequest request
    ) {
        TherapistDTO therapist = therapistManagementService.updateTherapist(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật therapist thành công!", therapist)
        );
    }

    ///     * DELETE /api/admin/therapists/{id}
     //Deactivate therapist

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deactivateTherapist(@PathVariable Long id) {
        therapistManagementService.deactivateTherapist(id);

        return ResponseEntity.ok(
                ApiResponse.success("Vô hiệu hóa therapist thành công!")
        );
    }

    /// POST /api/admin/therapists/{id}/services
     // Assign service to therapist

    @PostMapping("/{id}/services")
    public ResponseEntity<ApiResponse<String>> assignService(
            @PathVariable Long id,
            @Valid @RequestBody AssignServiceToTherapistRequest request
    ) {
        therapistManagementService.assignService(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Gán dịch vụ thành công!")
        );
    }


     ///DELETE /api/admin/therapists/{therapistId}/services/{serviceId}
     // Remove service from therapist

    @DeleteMapping("/{therapistId}/services/{serviceId}")
    public ResponseEntity<ApiResponse<String>> removeService(
            @PathVariable Long therapistId,
            @PathVariable Long serviceId
    ) {
        therapistManagementService.removeService(therapistId, serviceId);

        return ResponseEntity.ok(
                ApiResponse.success("Gỡ dịch vụ thành công!")
        );
    }
}