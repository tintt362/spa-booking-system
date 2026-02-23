package com.trongtin.spabooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trongtin.spabooking.dto.request.AnonymousBookingRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookingFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Complete booking flow test
     */
    @Test
    void testCompleteBookingFlow() throws Exception {
        // 1. Get services
        mockMvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").exists());

        // 2. Get available slots
        LocalDate futureDate = LocalDate.now().plusDays(3);
        mockMvc.perform(get("/api/services/1/available-slots")
                        .param("date", futureDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        // 3. Create booking
        AnonymousBookingRequest request = AnonymousBookingRequest.builder()
                .customerName("Integration Test User")
                .customerPhone("0901234567")
                .customerEmail("integration@test.com")
                .serviceId(1L)
                .therapistId(1L)
                .bookingDate(futureDate)
                .bookingTime("14:00")
                .note("Integration test")
                .build();

        String bookingResponse = mockMvc.perform(post("/api/bookings/anonymous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookingId").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract bookingId
        String bookingId = objectMapper.readTree(bookingResponse)
                .get("data")
                .get("bookingId")
                .asText();

        // 4. Check booking status
        mockMvc.perform(get("/api/bookings/" + bookingId + "/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        // 5. Verify slot is now booked (should fail to book again)
        mockMvc.perform(post("/api/bookings/anonymous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("BOOK_005"));
    }
}