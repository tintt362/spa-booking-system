package com.trongtin.spabooking.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnonymousBookingRequest {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 2, max = 100, message = "Họ tên từ 2-100 ký tự")
    private String customerName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    private String customerPhone;

    @Email(message = "Email không hợp lệ")
    private String customerEmail;

    @NotNull(message = "Vui lòng chọn dịch vụ")
    @Positive(message = "ID dịch vụ không hợp lệ")
    private Long serviceId;

    @Positive(message = "ID therapist không hợp lệ")
    private Long therapistId;

    @NotNull(message = "Vui lòng chọn ngày")
    @Future(message = "Ngày đặt lịch phải là ngày trong tương lai")
    private LocalDate bookingDate;

    @NotBlank(message = "Vui lòng chọn giờ")
    @Pattern(regexp = "^([01][0-9]|2[0-3]):[0-5][0-9]$", message = "Giờ không hợp lệ")
    private String bookingTime;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String note;

}


