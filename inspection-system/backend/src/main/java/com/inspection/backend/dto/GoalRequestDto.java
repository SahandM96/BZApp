package com.inspection.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalRequestDto {
    @NotBlank(message = "Goal name is required")
    private String name;
    private String description;
    private Long processId; // Optional
    private Double targetValue;
    private String targetMetricDescription;
    private Date startDate;
    private Date endDate;
    // createdByUserId will be from authenticated principal
    private Long responsibleUserId; // Optional
    private Long departmentId; // Optional, can be inferred from process if processId is given
    private String status; // Optional, can default in service
}
