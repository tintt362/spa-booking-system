package com.trongtin.spabooking.controller;

import com.trongtin.spabooking.dto.response.ApiResponse;
import com.trongtin.spabooking.dto.response.LoyaltyTransactionDTO;
import com.trongtin.spabooking.dto.response.UserDTO;
import com.trongtin.spabooking.service.LoyaltyService;
import com.trongtin.spabooking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService loyaltyService;
    private final UserService userService;


     //GET /api/user/loyalty
     //Get loyalty points summary
    @GetMapping
    public ResponseEntity<ApiResponse<UserDTO>> getLoyaltyInfo(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UserDTO user = userService.getProfile(userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.success(user));
    }

    // GET /api/user/loyalty/transactions
     // Get loyalty transaction history
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<Page<LoyaltyTransactionDTO>>> getTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LoyaltyTransactionDTO> transactions = loyaltyService.getTransactions(
                userDetails.getUsername(),
                pageable
        );

        return ResponseEntity.ok(ApiResponse.success(transactions));
    }
}