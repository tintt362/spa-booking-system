package com.trongtin.spabooking.service;


import com.trongtin.spabooking.contant.ErrorCode;
import com.trongtin.spabooking.dto.request.LoginRequest;
import com.trongtin.spabooking.dto.request.RegisterRequest;
import com.trongtin.spabooking.dto.response.*;
import com.trongtin.spabooking.entity.Gender;
import com.trongtin.spabooking.entity.*;
import com.trongtin.spabooking.exception.*;
import com.trongtin.spabooking.mapper.BookingMapper;
import com.trongtin.spabooking.repository.*;
import com.trongtin.spabooking.security.*;
import com.trongtin.spabooking.service.async.AsyncBookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final BookingMapper mapper;
    private final AsyncBookingService asyncService;

    //Register new user
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());

        // 1. Validate unique email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BookingException(
                    ErrorCode.AUTH_001,
                    "Email đã được sử dụng"
            );
        }

        // 2. Validate unique phone
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BookingException(
                    ErrorCode.AUTH_002,
                    "Số điện thoại đã được sử dụng"
            );
        }

        // 3. Validate password match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BookingException(
                    ErrorCode.VAL_001,
                    "Mật khẩu xác nhận không khớp"
            );
        }

        // 4. Create user
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender() != null
                        ? Gender.valueOf(request.getGender())
                        : null)
                .isVerified(false)
                .isActive(true)
                .loyaltyPoints(0)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created: id={}, email={}", savedUser.getId(), savedUser.getEmail());

        // 5. Create verification token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .user(savedUser)
                .token(token)
                .tokenType(TokenType.EMAIL_VERIFICATION)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        tokenRepository.save(verificationToken);

        // 6. Send verification email (async)
        sendVerificationEmail(savedUser, token);

        // 7. Generate JWT (even though not verified yet)
        String jwtToken = jwtService.generateToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        UserDTO userDTO = mapper.toUserDTO(savedUser);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(userDTO)
                .build();
    }

     //Login
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt: {}", request.getEmail());

        try {
            // 1. Authenticate
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

        } catch (Exception e) {
            log.warn("Login failed for email: {}", request.getEmail());
            throw new AuthenticationException("Email hoặc mật khẩu không đúng");
        }

        // 2. Get user
        User user = userRepository.findActiveByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException(
                        "Email hoặc mật khẩu không đúng"
                ));

        // 3. Check if verified (optional - allow login but limited features)
        if (!Boolean.TRUE.equals(user.getIsVerified())) {
            log.warn("User {} not verified yet", user.getEmail());
            // Don't block login, but user will have limited features
        }

        // 4. Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 5. Generate tokens
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        UserDTO userDTO = mapper.toUserDTO(user);

        log.info("Login successful: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(userDTO)
                .build();
    }

    //Verify email
    @Transactional
    public void verifyEmail(String token) {
        log.info("Verifying email with token: {}", token);

        VerificationToken verificationToken = tokenRepository
                .findByTokenAndTokenType(token, TokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new BookingException(
                        ErrorCode.AUTH_006,
                        "Token không hợp lệ"
                ));

        if (verificationToken.isUsed()) {
            throw new BookingException(
                    ErrorCode.AUTH_006,
                    "Token đã được sử dụng"
            );
        }

        if (verificationToken.isExpired()) {
            throw new BookingException(
                    ErrorCode.AUTH_007,
                    "Token đã hết hạn"
            );
        }

        // Mark user as verified
        User user = verificationToken.getUser();
        user.setIsVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        // Mark token as used
        verificationToken.setUsedAt(LocalDateTime.now());
        tokenRepository.save(verificationToken);

        log.info("Email verified: {}", user.getEmail());
    }


     // Refresh token
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");

        try {
            String email = jwtService.extractUsername(refreshToken);

            User user = userRepository.findActiveByEmail(email)
                    .orElseThrow(() -> new AuthenticationException("User not found"));

            if (!jwtService.validateToken(refreshToken, user)) {
                throw new AuthenticationException("Invalid refresh token");
            }

            String newAccessToken = jwtService.generateToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            UserDTO userDTO = mapper.toUserDTO(user);

            return AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getExpirationTime())
                    .user(userDTO)
                    .build();

        } catch (Exception e) {
            throw new AuthenticationException("Invalid refresh token");
        }
    }

    /**
     * Send verification email (async)
     */
    private void sendVerificationEmail(User user, String token) {
        // Async implementation will be added
        log.info("TODO: Send verification email to {}", user.getEmail());
    }
}