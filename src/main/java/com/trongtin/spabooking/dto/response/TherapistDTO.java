package com.trongtin.spabooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TherapistDTO {

    private Long id;
    private String fullName;
    private String phone;
    private String email;
    private String employeeCode;
    private String avatarUrl;
    private String skillLevel;
    private Integer yearsExperience;
    private Boolean isExpert;
}
