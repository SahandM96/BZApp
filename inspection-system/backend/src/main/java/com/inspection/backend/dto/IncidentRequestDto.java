package com.inspection.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentRequestDto {
    private Long departmentId; // Optional

    @NotBlank(message = "Incident description is required")
    private String description;

    @NotNull(message = "Incident date is required")
    private Date incidentDate;

    // reportedByUserId will be from authenticated principal
    private String status; // Optional, can default in service
    private String severity; // Optional
}
