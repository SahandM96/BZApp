package com.inspection.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// Base DTO for KPI, used in ProcessDto and GoalDto to avoid circular dependencies
// or when only minimal KPI info is needed.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpiBaseDto {
    private Long id;
    private String name;
    private Double targetValue;
    private Double actualValue;
    private String unit;
}
