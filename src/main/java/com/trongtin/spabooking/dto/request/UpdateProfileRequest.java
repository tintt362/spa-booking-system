package com.trongtin.spabooking.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(min = 2, max = 100)
    private String fullName;

    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$")
    private String phone;

    @Past
    private LocalDate dateOfBirth;

    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$")
    private String gender;

    @Positive
    private Long preferredTherapistId;
}