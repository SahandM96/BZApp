package com.inspection.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDto {
    private Long id;
    private String name;
    private Long parentId;
    private String parentName; // For display
    private Long managerUserId;
    private String managerUserName; // For display
    // private List<UserDto> users; // Could be too much data, typically not included directly
    // private List<ProcessDto> processes; // Same as above
    private int numberOfUsers; // Example: derived data
    private int numberOfProcesses; // Example: derived data
}
