package com.trongtin.spabooking.dto.request;


import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBookingRequest {

    @NotNull(message = "Vui lòng chọn dịch vụ")
    @Positive
    private Long serviceId;

    @Positive
    private Long therapistId;

    @NotNull(message = "Vui lòng chọn ngày")
    @Future(message = "Ngày đặt lịch phải là ngày trong tương lai")
    private LocalDate bookingDate;

    @NotBlank(message = "Vui lòng chọn giờ")
    @Pattern(regexp = "^([01][0-9]|2[0-3]):[0-5][0-9]$")
    private String bookingTime;

    @Size(max = 500)
    private String note;

    @Builder.Default
    private Boolean usePoints = false;

    @Min(value = 0, message = "Số điểm không hợp lệ")
    private Integer pointsToRedeem;
}
