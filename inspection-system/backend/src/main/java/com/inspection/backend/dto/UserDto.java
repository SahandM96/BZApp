package com.inspection.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    // Password should not be in DTOs sent to client
    private String email;
    private String firstName;
    private String lastName;
    private Long departmentId; // ID of the department
    private String departmentName; // Name of the department for display
    private String roleName;     // Name of the role for display
    private boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
