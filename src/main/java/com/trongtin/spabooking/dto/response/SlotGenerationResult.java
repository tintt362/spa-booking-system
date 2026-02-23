package com.trongtin.spabooking.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotGenerationResult {

    private Integer totalGenerated;
    private Integer skipped;
    private Integer overwritten;
    private String message;
}
