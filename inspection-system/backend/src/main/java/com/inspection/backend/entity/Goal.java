package com.inspection.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.time.Instant;
import java.util.Date; // Using java.util.Date for @Temporal
import java.util.List;

@Entity
@Table(name = "goal")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"kpis", "process", "createdBy", "responsibleUser", "department"}) // Exclude collections and LAZY/EAGER direct relations
@ToString(exclude = {"kpis", "process", "createdBy", "responsibleUser", "department"}) // Exclude collections
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Lob
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id") // Can be null if goal is not directly tied to a single process
    private Process process;

    @Column(name = "target_value")
    private Double targetValue;

    @Column(name = "target_metric_description")
    private String targetMetricDescription;

    @Temporal(TemporalType.DATE)
    @Column(name = "start_date")
    private Date startDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "end_date")
    private Date endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_user_id")
    private User responsibleUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id") // A goal can be associated with a department directly
    private Department department;

    @Column(length = 50)
    private String status; // e.g., 'ACTIVE', 'COMPLETED', 'ON_HOLD'

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<KPI> kpis;

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
