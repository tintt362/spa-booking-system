package com.trongtin.spabooking.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.trongtin.spabooking.dto.request.LoginRequest;
import com.trongtin.spabooking.dto.request.RegisterRequest;
import com.trongtin.spabooking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());


    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    /**
     * Test complete auth flow: Register -> Login -> Access Protected
     */
    @Test
    void testCompleteAuthFlow() throws Exception {
        // 1. Register
        RegisterRequest registerRequest = RegisterRequest.builder()
                .fullName("Test User")
                .email("test@example.com")
                .phone("0901234567")
                .password("Test@123")
                .confirmPassword("Test@123")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender("MALE")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"));

        // 2. Login
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("Test@123")
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andReturn();

        // Extract token
        String responseBody = loginResult.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(responseBody)
                .get("data")
                .get("accessToken")
                .asText();

        // 3. Access protected endpoint
        mockMvc.perform(get("/api/user/profile")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        // 4. Try to access without token
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isForbidden());

        // 5. Try to access with invalid token
        mockMvc.perform(get("/api/user/profile")
                        .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isForbidden());
    }

    /**
     * Test registration validation
     */
    @Test
    void testRegisterValidation() throws Exception {
        // Invalid email
        RegisterRequest request1 = RegisterRequest.builder()
                .fullName("Test")
                .email("invalid-email")
                .phone("0901234567")
                .password("Test@123")
                .confirmPassword("Test@123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        // Password too short
        RegisterRequest request2 = RegisterRequest.builder()
                .fullName("Test")
                .email("test@example.com")
                .phone("0901234567")
                .password("Test@1")
                .confirmPassword("Test@1")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test duplicate registration
     */
    @Test
    void testDuplicateRegistration() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Test User")
                .email("duplicate@example.com")
                .phone("0901234567")
                .password("Test@123")
                .confirmPassword("Test@123")
                .build();

        // First registration - success
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Second registration - should fail
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("AUTH_001"));
    }

    /**
     * Test login with wrong credentials
     */
    @Test
    void testLoginWrongCredentials() throws Exception {
        // Register user first
        RegisterRequest registerRequest = RegisterRequest.builder()
                .fullName("Test User")
                .email("testlogin@example.com")
                .phone("0901234567")
                .password("Test@123")
                .confirmPassword("Test@123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // Login with wrong password
        LoginRequest loginRequest = LoginRequest.builder()
                .email("testlogin@example.com")
                .password("WrongPassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}