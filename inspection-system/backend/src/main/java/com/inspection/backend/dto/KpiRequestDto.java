package com.inspection.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpiRequestDto {
    @NotBlank(message = "KPI name is required")
    private String name;
    private String description;

    @NotNull(message = "Goal ID is required for KPI")
    private Long goalId;

    private Double targetValue;
    private Double actualValue; // Usually updated via a separate endpoint
    private String unit;
    private String measurementFrequency;
    private Long responsibleUserId; // Optional
}
