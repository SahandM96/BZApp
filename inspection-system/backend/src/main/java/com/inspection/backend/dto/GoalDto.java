package com.inspection.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalDto {
    private Long id;
    private String name;
    private String description;
    private Long processId;
    private String processName;
    private Double targetValue;
    private String targetMetricDescription;
    private Date startDate;
    private Date endDate;
    private Long createdByUserId;
    private String createdByUserName;
    private Long responsibleUserId;
    private String responsibleUserName;
    private Long departmentId;
    private String departmentName;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private List<KpiBaseDto> kpis; // Base DTO for KPIs
}
