package com.trongtin.spabooking.controller;

import com.trongtin.spabooking.dto.response.ApiResponse;
import com.trongtin.spabooking.dto.response.AvailableSlotDTO;
import com.trongtin.spabooking.dto.response.ServiceDTO;
import com.trongtin.spabooking.entity.Service;
import com.trongtin.spabooking.exception.ResourceNotFoundException;
import com.trongtin.spabooking.mapper.BookingMapper;
import com.trongtin.spabooking.repository.*;
import com.trongtin.spabooking.service.BookingService;
import com.trongtin.spabooking.service.ServiceCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceRepository serviceRepository;
    private final BookingService bookingService;
    private final BookingMapper mapper;
    private final ServiceCacheService serviceCacheService;


     //GET /api/services
    // Get all active services
     @Operation(
             summary = "Get all active services",
             description = """
            Retrieve all active services with pricing and details.
            Services are grouped by category: MASSAGE, FACIAL, BODY_TREATMENT, SPA_PACKAGE.
            
            **Response includes:**
            - Service name, description, duration
            - Original price and discount price (if applicable)
            - Service category and display order
            """
     )
     @ApiResponses(value = {
             @io.swagger.v3.oas.annotations.responses.ApiResponse(
                     responseCode = "200",
                     description = "Services retrieved successfully",
                     content = @Content(
                             mediaType = "application/json",
                             examples = @ExampleObject(value = """
                    {
                        "success": true,
                        "data": [
                            {
                                "id": 1,
                                "name": "Body Massage",
                                "slug": "body-massage",
                                "description": "Full body relaxation massage",
                                "durationMinutes": 90,
                                "price": 500000,
                                "discountPrice": null,
                                "category": "MASSAGE",
                                "displayOrder": 1
                            },
                            {
                                "id": 2,
                                "name": "Foot Massage",
                                "slug": "foot-massage",
                                "description": "Relaxing foot reflexology",
                                "durationMinutes": 60,
                                "price": 300000,
                                "discountPrice": 250000,
                                "category": "MASSAGE",
                                "displayOrder": 2
                            }
                        ]
                    }
                    """)
                     )
             )
     })
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceDTO>>> getAllServices() {

         // 1. Try cache
         List<ServiceDTO> cached = serviceCacheService.getAll();
         if (cached != null) {
             return ResponseEntity.ok(ApiResponse.success(cached));
         }

         // 2. DB
         List<Service> services = serviceRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
         List<ServiceDTO> dtos = services.stream()
                 .map(mapper::toServiceDTO)
                 .collect(Collectors.toList());
         // 3. Set cache
         serviceCacheService.setAll(dtos);



        return ResponseEntity.ok(ApiResponse.success(dtos));
    }


     // GET /api/services/{id}
     //Get service details
     @Operation(
             summary = "Get service by ID",
             description = "Retrieve detailed information about a specific service"
     )
     @ApiResponses(value = {
             @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Service found"),
             @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Service not found")
     })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceDTO>> getService(@PathVariable Long id) {
         // 1. Cache
         ServiceDTO cached = serviceCacheService.getById(id);
         if (cached != null) {
             return ResponseEntity.ok(ApiResponse.success(cached));
         }

         // 2. DB
         Service service = serviceRepository.findById(id)
                 .orElseThrow(() -> new ResourceNotFoundException("Service"));

         ServiceDTO dto = mapper.toServiceDTO(service);

         // 3. Cache
         serviceCacheService.setById(id, dto);
        return ResponseEntity.ok(ApiResponse.success(mapper.toServiceDTO(service)));
    }


     //GET /api/services/{id}/available-slots
     // Get available time slots
     @Operation(
             summary = "Get available time slots",
             description = """
            Get available booking slots for a specific service and date.
            
            **How it works:**
            - Generates slots from 8:00 AM to 8:00 PM
            - Each slot is 30 minutes apart
            - Automatically assigns available therapist
            - Excludes already booked slots
            - Considers service duration
            
            **Therapist Assignment:**
            - Prioritized by skill level (EXPERT > INTERMEDIATE > BEGINNER)
            - Checks therapist availability
            - Shows therapist name and ID for each slot
            """
     )
     @ApiResponses(value = {
             @io.swagger.v3.oas.annotations.responses.ApiResponse(
                     responseCode = "200",
                     description = "Available slots retrieved",
                     content = @Content(
                             mediaType = "application/json",
                             examples = @ExampleObject(value = """
                    {
                        "success": true,
                        "data": [
                            {
                                "time": "08:00",
                                "therapistId": 1,
                                "therapistName": "Nguyễn Thị Mai",
                                "skillLevel": "EXPERT"
                            },
                            {
                                "time": "08:30",
                                "therapistId": 2,
                                "therapistName": "Trần Thị Lan",
                                "skillLevel": "INTERMEDIATE"
                            },
                            {
                                "time": "09:00",
                                "therapistId": 1,
                                "therapistName": "Nguyễn Thị Mai",
                                "skillLevel": "EXPERT"
                            }
                        ]
                    }
                    """)
                     )
             ),
             @io.swagger.v3.oas.annotations.responses.ApiResponse(
                     responseCode = "400",
                     description = "Invalid date (past date, Sunday, etc.)"
             ),
             @io.swagger.v3.oas.annotations.responses.ApiResponse(
                     responseCode = "404",
                     description = "Service not found"
             )
     })
    @GetMapping("/{id}/available-slots")
    public ResponseEntity<ApiResponse<List<AvailableSlotDTO>>> getAvailableSlots(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<AvailableSlotDTO> slots = bookingService.getAvailableSlots(id, date);

        return ResponseEntity.ok(ApiResponse.success(slots));
    }
}