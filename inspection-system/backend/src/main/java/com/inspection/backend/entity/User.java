package com.inspection.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"createdProcesses", "createdGoals", "responsibleForGoals", "responsibleForKpis", "reportedIncidents", "managedDepartments", "department", "role"}) // Exclude collections and direct EAGER/LAZY relations
@ToString(exclude = {"createdProcesses", "createdGoals", "responsibleForGoals", "responsibleForKpis", "reportedIncidents", "managedDepartments", "department", "role"}) // Exclude collections
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.EAGER) // Roles are often needed, EAGER can be acceptable
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "is_active", nullable = false)
    @Builder.Default // For Lombok builder default value
    private boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    private List<Process> createdProcesses;

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    private List<Goal> createdGoals;

    @OneToMany(mappedBy = "responsibleUser", fetch = FetchType.LAZY)
    private List<Goal> responsibleForGoals;

    @OneToMany(mappedBy = "responsibleUser", fetch = FetchType.LAZY)
    private List<KPI> responsibleForKpis;

    @OneToMany(mappedBy = "reportedByUser", fetch = FetchType.LAZY)
    private List<Incident> reportedIncidents;

    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    private List<Department> managedDepartments;


    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
