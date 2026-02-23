package com.trongtin.spabooking.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInfoDTO {

    private Long totalBookings;
    private Long completedBookings;
    private Long cancelledBookings;
    private java.math.BigDecimal totalSpent;
    private Integer loyaltyPoints;
    private String membershipTier;
}
