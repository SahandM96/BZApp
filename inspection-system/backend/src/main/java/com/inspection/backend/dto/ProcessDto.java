package com.inspection.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessDto {
    private Long id;
    private String name;
    private String description;
    private Long departmentId;
    private String departmentName;
    private Long createdByUserId;
    private String createdByUserName;
    private String status;
    private String inputs;
    private String outputs;
    private Instant createdAt;
    private Instant updatedAt;
    private List<GoalBaseDto> goals; // Using a base DTO to avoid circular dependencies if GoalDto contains ProcessDto
    private List<KpiBaseDto> kpis;   // Using a base DTO
}
