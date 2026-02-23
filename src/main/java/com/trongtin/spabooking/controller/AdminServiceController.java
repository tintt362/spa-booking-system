package com.trongtin.spabooking.controller;

import com.trongtin.spabooking.dto.request.CreateServiceRequest;
import com.trongtin.spabooking.dto.request.UpdateServiceRequest;
import com.trongtin.spabooking.dto.response.ApiResponse;
import com.trongtin.spabooking.dto.response.ServiceDTO;
import com.trongtin.spabooking.service.AdminServiceManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/services")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminServiceController {

    private final AdminServiceManagementService serviceManagementService;


     //GET /api/admin/services
     // Get all services (including inactive)
     @Tag(name = "Admin - Services")
     @Operation(
             summary = "Get all services (including inactive)",
             description = "Admin view of all services with option to include inactive ones"
     )
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceDTO>>> getAllServices(
            @RequestParam(required = false) Boolean includeInactive
    ) {
        List<ServiceDTO> services = serviceManagementService.getAllServices(
                includeInactive != null && includeInactive
        );

        return ResponseEntity.ok(ApiResponse.success(services));
    }


     // POST /api/admin/services
     // Create new service
     @Tag(name = "Admin - Services")
     @Operation(summary = "Create new service")
    @PostMapping
    public ResponseEntity<ApiResponse<ServiceDTO>> createService(
            @Valid @RequestBody CreateServiceRequest request
    ) {
        ServiceDTO service = serviceManagementService.createService(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo dịch vụ thành công!", service));
    }


      // PUT /api/admin/services/{id}
     // Update service
      @Tag(name = "Admin - Services")
      @Operation(summary = "Update service")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceDTO>> updateService(
            @PathVariable Long id,
            @Valid @RequestBody UpdateServiceRequest request
    ) {
        ServiceDTO service = serviceManagementService.updateService(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật dịch vụ thành công!", service)
        );
    }


     // DELETE /api/admin/services/{id}
    // Soft delete service
     @Tag(name = "Admin - Services")
     @Operation(
             summary = "Deactivate service",
             description = "Soft delete - service becomes inactive but data is preserved"
     )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteService(@PathVariable Long id) {
        serviceManagementService.deleteService(id);

        return ResponseEntity.ok(
                ApiResponse.success("Xóa dịch vụ thành công!")
        );
    }


     // PUT /api/admin/services/{id}/activate
     //Activate service

    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<String>> activateService(@PathVariable Long id) {
        serviceManagementService.activateService(id);

        return ResponseEntity.ok(
                ApiResponse.success("Kích hoạt dịch vụ thành công!")
        );
    }
}