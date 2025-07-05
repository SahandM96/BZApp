package com.inspection.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "kpi")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"goal", "responsibleUser"}) // Exclude LAZY/EAGER direct relations
@ToString(exclude = {"goal", "responsibleUser"})
public class KPI {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Lob
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @Column(name = "target_value")
    private Double targetValue; // Use Double to allow nulls if target is not set

    @Column(name = "actual_value")
    private Double actualValue; // Use Double to allow nulls

    @Column(length = 50)
    private String unit;

    @Column(name = "measurement_frequency", length = 50)
    private String measurementFrequency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_user_id")
    private User responsibleUser;

    @Column(name = "last_updated_value_date")
    private Instant lastUpdatedValueDate;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // If a direct link from KPI to Process is still needed, uncomment the following:
    /*
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id")
    @ToString.Exclude // Add to avoid issues with Lombok if Process has a list of KPIs
    @EqualsAndHashCode.Exclude
    private Process process;
    */

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
