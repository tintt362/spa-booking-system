package com.trongtin.spabooking.controller;

import com.trongtin.spabooking.dto.request.RefreshTokenRequest;
import com.trongtin.spabooking.dto.request.*;
import com.trongtin.spabooking.dto.response.AuthResponse;
import com.trongtin.spabooking.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and registration APIs")
public class AuthController {

    private final AuthService authService;


    //POST /api/auth/register
    //Register new user
    @Operation(
            summary = "Register new user",
            description = "Register a new user account. Email verification is required before login."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully. Verification email sent.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                    {
                        "success": true,
                        "message": "Registration successful. Please check your email to verify your account.",
                        "data": null
                    }
                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error or duplicate email/phone",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "errorCode": "DUPLICATE_EMAIL",
                        "message": "Email already exists",
                        "errors": []
                    }
                    """)
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity<com.trongtin.spabooking.dto.response.ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        AuthResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(com.trongtin.spabooking.dto.response.ApiResponse.success(
                        "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.",
                        response
                ));
    }


    //POST /api/auth/login
    // Login
    @Operation(
            summary = "User login",
            description = "Authenticate user and receive JWT access token and refresh token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(value = """
                    {
                        "success": true,
                        "data": {
                            "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                            "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                            "tokenType": "Bearer",
                            "expiresIn": 3600000,
                            "user": {
                                "id": 1,
                                "fullName": "Nguyễn Văn A",
                                "email": "user@example.com",
                                "loyaltyPoints": 500,
                                "membershipTier": "SILVER"
                            }
                        }
                    }
                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials or account not verified",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "errorCode": "INVALID_CREDENTIALS",
                        "message": "Invalid email or password"
                    }
                    """)
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<com.trongtin.spabooking.dto.response.ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(
                com.trongtin.spabooking.dto.response.ApiResponse.success("Đăng nhập thành công!", response)
        );
    }


    //GET /api/auth/verify-email?token=xxx
    //Verify email
    @GetMapping("/verify-email")
    public ResponseEntity<com.trongtin.spabooking.dto.response.ApiResponse<String>> verifyEmail(
            @RequestParam String token
    ) {
        authService.verifyEmail(token);

        return ResponseEntity.ok(
                com.trongtin.spabooking.dto.response.ApiResponse.success("Xác thực email thành công!")
        );
    }


    //POST /api/auth/refresh-token
    //Refresh access token

    @Operation(
            summary = "Refresh access token",
            description = "Get a new access token using refresh token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token"
            )
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<com.trongtin.spabooking.dto.response.ApiResponse<AuthResponse>> refreshToken(
            @RequestBody RefreshTokenRequest request
    ) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());

        return ResponseEntity.ok(
                com.trongtin.spabooking.dto.response.ApiResponse.success(response)
        );
    }
    @PostMapping("/logout")
    public ResponseEntity<com.trongtin.spabooking.dto.response.ApiResponse<String>> logout(
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.substring(7);
        authService.logout(token);

        return ResponseEntity.ok(
                com.trongtin.spabooking.dto.response.ApiResponse.success("Logout thành công")
        );
    }

}
