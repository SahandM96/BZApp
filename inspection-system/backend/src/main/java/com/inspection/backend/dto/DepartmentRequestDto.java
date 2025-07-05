package com.inspection.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRequestDto {
    @NotBlank(message = "Department name is required")
    private String name;
    private Long parentId; // Optional
    private Long managerUserId; // Optional
}
