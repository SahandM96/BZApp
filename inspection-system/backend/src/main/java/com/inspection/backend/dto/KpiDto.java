package com.inspection.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpiDto {
    private Long id;
    private String name;
    private String description;
    private Long goalId;
    private String goalName;
    private Double targetValue;
    private Double actualValue;
    private String unit;
    private String measurementFrequency;
    private Long responsibleUserId;
    private String responsibleUserName;
    private Instant lastUpdatedValueDate;
    private Instant createdAt;
    private Instant updatedAt;
}
