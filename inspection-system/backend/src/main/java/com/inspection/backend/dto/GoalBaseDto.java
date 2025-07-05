package com.inspection.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

// Base DTO for Goal, used in ProcessDto to avoid circular dependencies
// or when only minimal Goal info is needed.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalBaseDto {
    private Long id;
    private String name;
    private String status;
    private Date endDate;
    private Double targetValue;
    private String targetMetricDescription;
}
