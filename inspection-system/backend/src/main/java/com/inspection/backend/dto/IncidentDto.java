package com.inspection.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentDto {
    private Long id;
    private Long departmentId;
    private String departmentName;
    private String description;
    private Date incidentDate;
    private Long reportedByUserId;
    private String reportedByUserName;
    private String status;
    private String severity;
    private Instant createdAt;
    private Instant updatedAt;
}
