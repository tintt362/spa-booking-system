package com.trongtin.spabooking.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelBookingRequest {

    @NotBlank(message = "Vui lòng nhập lý do hủy")
    @Size(min = 10, max = 500, message = "Lý do hủy từ 10-500 ký tự")
    private String reason;
}