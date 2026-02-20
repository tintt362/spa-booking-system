package com.trongtin.spabooking.service;

import com.trongtin.spabooking.dto.*;
import com.trongtin.spabooking.dto.request.*;
import com.trongtin.spabooking.dto.response.*;
import com.trongtin.spabooking.entity.*;
import com.trongtin.spabooking.exception.BookingException;
import com.trongtin.spabooking.exception.ResourceNotFoundException;
import com.trongtin.spabooking.mapper.BookingMapper;
import com.trongtin.spabooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BookingMapper mapper;


    // Get user profile
    @Transactional(readOnly = true)
    public UserDTO getProfile(String email) {
        User user = userRepository.findActiveByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User"));

        return mapper.toUserDTO(user);
    }

    /**
     * Update profile
     */
    @Transactional
    public UserDTO updateProfile(String email, UpdateProfileRequest request) {
        log.info("Updating profile for user: {}", email);

        User user = userRepository.findActiveByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User"));

        // Update fields
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            // Check phone uniqueness
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new BookingException(
                        "PHONE_EXISTS",
                        "Số điện thoại đã được sử dụng"
                );
            }
            user.setPhone(request.getPhone());
        }

        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }

        if (request.getGender() != null) {
            user.setGender(Gender.valueOf(request.getGender()));
        }

        if (request.getPreferredTherapistId() != null) {
            // Will set after validating therapist exists
            // therapist validation logic here
        }

        User updated = userRepository.save(user);
        log.info("Profile updated: {}", email);

        return mapper.toUserDTO(updated);
    }

    /**
     * Change password
     */
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        log.info("Changing password for user: {}", email);

        User user = userRepository.findActiveByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BookingException(
                    "INVALID_PASSWORD",
                    "Mật khẩu hiện tại không đúng"
            );
        }

        // Verify new password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BookingException(
                    "PASSWORD_MISMATCH",
                    "Mật khẩu xác nhận không khớp"
            );
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed: {}", email);
    }
}
