package com.trongtin.spabooking.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailDTO {

    private UserDTO user;
    private CustomerInfoDTO customerInfo;
    private List<BookingResponse> recentBookings;
    private List<LoyaltyTransactionDTO> recentTransactions;
}