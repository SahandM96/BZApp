package com.inspection.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessRequestDto {
    @NotBlank(message = "Process name is required")
    private String name;
    private String description;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    // createdByUserId will be set from the authenticated user in the service/controller
    // private Long createdByUserId;

    private String status; // Optional, can default in service
    private String inputs;
    private String outputs;
}
