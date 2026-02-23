package com.trongtin.spabooking.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.trongtin.spabooking.dto.request.ChangePasswordRequest;
import com.trongtin.spabooking.dto.request.LoginRequest;
import com.trongtin.spabooking.dto.request.UpdateProfileRequest;
import com.trongtin.spabooking.entity.User;
import com.trongtin.spabooking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserFeaturesTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Helper: Create user and get token
     */
    private String getAuthToken() throws Exception {
        // Create user
        User user = User.builder()
                .fullName("Test User")
                .email("usertest@example.com")
                .phone("0901234567")
                .passwordHash(passwordEncoder.encode("Test@123"))
                .isVerified(true)
                .isActive(true)
                .build();

        userRepository.save(user);

        // Login
        LoginRequest loginRequest = LoginRequest.builder()
                .email("usertest@example.com")
                .password("Test@123")
                .build();

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response)
                .get("data")
                .get("accessToken")
                .asText();
    }

    /**
     * Test get profile
     */
    @Test
    void testGetProfile() throws Exception {
        String token = getAuthToken();

        mockMvc.perform(get("/api/user/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("usertest@example.com"))
                .andExpect(jsonPath("$.data.loyaltyPoints").value(0));
    }

    /**
     * Test update profile
     */
    @Test
    void testUpdateProfile() throws Exception {
        String token = getAuthToken();

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .fullName("Updated Name")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender("FEMALE")
                .build();

        mockMvc.perform(put("/api/user/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Updated Name"));
    }

    /**
     * Test change password
     */
    @Test
    void testChangePassword() throws Exception {
        String token = getAuthToken();

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("Test@123")
                .newPassword("NewPass@123")
                .confirmPassword("NewPass@123")
                .build();

        mockMvc.perform(put("/api/user/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Try login with new password
        LoginRequest loginRequest = LoginRequest.builder()
                .email("usertest@example.com")
                .password("NewPass@123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }
}