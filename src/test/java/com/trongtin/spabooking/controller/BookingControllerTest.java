package com.trongtin.spabooking.controller;



import com.fasterxml.jackson.databind.ObjectMapper;

import com.trongtin.spabooking.dto.request.AnonymousBookingRequest;
import com.trongtin.spabooking.repository.BookingSlotRepository;
import com.trongtin.spabooking.repository.ServiceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@Testcontainers
class BookingControllerTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER =  new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRE_SQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRE_SQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRE_SQL_CONTAINER::getPassword);

        // optional (Spring Boot thường tự detect driver từ jdbcUrl)
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");

        // optional: Hibernate dialect (Boot đa số tự suy ra, nhưng có thể set để chắc)
        registry.add("spring.jpa.properties.hibernate.dialect",
                () -> "org.hibernate.dialect.PostgreSQLDialect");
    }
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private BookingSlotRepository slotRepository;

    @Test
    void testCreateAnonymousBooking_Success() throws Exception {
        AnonymousBookingRequest request = AnonymousBookingRequest.builder()
                .customerName("Test User")
                .customerPhone("0901234567")
                .customerEmail("test@example.com")
                .serviceId(1L)
                .bookingDate(LocalDate.now().plusDays(3))
                .bookingTime("14:00")
                .note("Test booking")
                .build();

        mockMvc.perform(post("/api/bookings/anonymous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookingId").exists())
                .andExpect(jsonPath("$.data.customerName").value("Test User"));
    }

    @Test
    void testCreateAnonymousBooking_InvalidPhone() throws Exception {
        AnonymousBookingRequest request = AnonymousBookingRequest.builder()
                .customerName("Test User")
                .customerPhone("123") // Invalid
                .customerEmail("test@example.com")
                .serviceId(1L)
                .bookingDate(LocalDate.now().plusDays(3))
                .bookingTime("14:00")
                .build();

        mockMvc.perform(post("/api/bookings/anonymous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void testCreateAnonymousBooking_SlotNotAvailable() throws Exception {
        // Create a booking first
        AnonymousBookingRequest request1 = createValidRequest();
        mockMvc.perform(post("/api/bookings/anonymous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Try to book same slot again
        AnonymousBookingRequest request2 = createValidRequest();
        mockMvc.perform(post("/api/bookings/anonymous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("BOOK_005"));
    }

    @Test
    void testGetAvailableSlots() throws Exception {
        mockMvc.perform(get("/api/services/1/available-slots")
                        .param("date", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    private AnonymousBookingRequest createValidRequest() {
        return AnonymousBookingRequest.builder()
                .customerName("Test User")
                .customerPhone("0901234567")
                .customerEmail("test@example.com")
                .serviceId(1L)
                .therapistId(1L)
                .bookingDate(LocalDate.now().plusDays(3))
                .bookingTime("14:00")
                .build();
    }
}