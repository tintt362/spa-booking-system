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
public class AssignServiceToTherapistRequest {

    @NotNull
    @Positive
    private Long serviceId;

    @NotNull
    @Pattern(regexp = "^(BEGINNER|INTERMEDIATE|EXPERT)$")
    private String skillLevel;

    @Min(0)
    @Max(30)
    private Integer yearsExperience;

    private Boolean isPrimaryService = false;
}