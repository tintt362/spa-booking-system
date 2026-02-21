package com.trongtin.spabooking.dto.request;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTherapistRequest {

    @NotBlank
    @Size(min = 2, max = 100)
    private String fullName;

    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$")
    private String phone;

    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^TH[0-9]{3}$", message = "Mã nhân viên phải theo format THxxx")
    private String employeeCode;

    private String avatarUrl;
}