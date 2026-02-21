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
public class UpdateTherapistRequest {

    @Size(min = 2, max = 100)
    private String fullName;

    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$")
    private String phone;

    @Email
    private String email;

    private String avatarUrl;
}
