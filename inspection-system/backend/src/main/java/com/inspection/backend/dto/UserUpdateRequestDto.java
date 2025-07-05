package com.inspection.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDto {
    // Username might not be updatable, or updatable by admins only.
    // For now, let's assume it can be updated.
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    // Password update should be handled separately or with specific fields like newPassword, oldPassword
    // For simplicity, not including direct password field here. A dedicated endpoint might be better.

    @Email(message = "Email should be valid")
    private String email;

    private String firstName;
    private String lastName;
    private Long departmentId;
    private String roleName; // Role might be updatable by an admin
    private Boolean isActive;
}
