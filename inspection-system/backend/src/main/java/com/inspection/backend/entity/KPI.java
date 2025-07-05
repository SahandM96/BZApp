package com.inspection.backend.entity;

import javax.persistence.*;

@Entity
public class KPI {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private double targetValue;
    private double actualValue;
    private String unit;

    @ManyToOne
    @JoinColumn(name = "process_id")
    private Process process;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getTargetValue() { return targetValue; }
    public void setTargetValue(double targetValue) { this.targetValue = targetValue; }
    public double getActualValue() { return actualValue; }
    public void setActualValue(double actualValue) { this.actualValue = actualValue; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Process getProcess() { return process; }
    public void setProcess(Process process) { this.process = process; }
}
