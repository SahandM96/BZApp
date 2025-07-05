package com.inspection.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.time.Instant;
import java.util.Date; // Using java.util.Date for @Temporal

@Entity
@Table(name = "incident")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"department", "reportedByUser"}) // Exclude LAZY/EAGER direct relations
@ToString(exclude = {"department", "reportedByUser"})
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Lob
    @Column(nullable = false)
    private String description;

    @Temporal(TemporalType.DATE)
    @Column(name = "incident_date", nullable = false)
    private Date incidentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_user_id")
    private User reportedByUser;

    @Column(length = 50)
    private String status; // e.g., 'OPEN', 'INVESTIGATING', 'RESOLVED', 'CLOSED'

    @Column(length = 50)
    private String severity; // e.g., 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

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
