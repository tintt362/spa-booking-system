package com.trongtin.spabooking.controller;


import com.trongtin.spabooking.dto.request.AdjustPointsRequest;
import com.trongtin.spabooking.dto.response.*;
import com.trongtin.spabooking.dto.response.UserDTO;
import com.trongtin.spabooking.service.AdminUserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminUserController {

    private final AdminUserManagementService userManagementService;


     //GET /api/admin/users
     //Get all users with pagination
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String tier,
            @RequestParam(required = false) Boolean verified,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<UserDTO> users = userManagementService.getUsers(
                search, tier, verified, pageable
        );

        return ResponseEntity.ok(ApiResponse.success(users));
    }


     // GET /api/admin/users/{id}
     // Get user details
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDetailDTO>> getUserDetail(@PathVariable Long id) {
        UserDetailDTO detail = userManagementService.getUserDetail(id);

        return ResponseEntity.ok(ApiResponse.success(detail));
    }


     // PUT /api/admin/users/{id}/verify
     // Manually verify user
    @PutMapping("/{id}/verify")
    public ResponseEntity<ApiResponse<String>> verifyUser(@PathVariable Long id) {
        userManagementService.verifyUser(id);

        return ResponseEntity.ok(
                ApiResponse.success("Xác thực user thành công!")
        );
    }


    // PUT /api/admin/users/{id}/deactivate
   //  Deactivate user account

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateUser(@PathVariable Long id) {
        userManagementService.deactivateUser(id);

        return ResponseEntity.ok(
                ApiResponse.success("Vô hiệu hóa user thành công!")
        );
    }


     //PUT /api/admin/users/{id}/activate
     // Activate user account

    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<String>> activateUser(@PathVariable Long id) {
        userManagementService.activateUser(id);

        return ResponseEntity.ok(
                ApiResponse.success("Kích hoạt user thành công!")
        );
    }


     //POST /api/admin/users/{id}/points
     // Adjust user loyalty points
    @PostMapping("/{id}/points")
    public ResponseEntity<ApiResponse<String>> adjustPoints(
            @PathVariable Long id,
            @RequestBody AdjustPointsRequest request
    ) {
        userManagementService.adjustPoints(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Điều chỉnh điểm thành công!")
        );
    }
}
