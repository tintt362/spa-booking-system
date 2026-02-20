package com.trongtin.spabooking.controller;

import com.trongtin.spabooking.dto.request.RefreshTokenRequest;
import com.trongtin.spabooking.dto.request.*;
import com.trongtin.spabooking.dto.response.*;
import com.trongtin.spabooking.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    //POST /api/auth/register
    //Register new user
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        AuthResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.",
                        response
                ));
    }


    //POST /api/auth/login
    // Login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success("Đăng nhập thành công!", response)
        );
    }


    //GET /api/auth/verify-email?token=xxx
    //Verify email
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<String>> verifyEmail(
            @RequestParam String token
    ) {
        authService.verifyEmail(token);

        return ResponseEntity.ok(
                ApiResponse.success("Xác thực email thành công!")
        );
    }


    //POST /api/auth/refresh-token
    //Refresh access token
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestBody RefreshTokenRequest request
    ) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

}
