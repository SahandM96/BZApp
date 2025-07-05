package com.inspection.backend.entity;

import javax.persistence.*;
import java.util.List;

@Entity
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Department parent;

    @OneToMany(mappedBy = "parent")
    private List<Department> subDepartments;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Department getParent() { return parent; }
    public void setParent(Department parent) { this.parent = parent; }
    public List<Department> getSubDepartments() { return subDepartments; }
    public void setSubDepartments(List<Department> subDepartments) { this.subDepartments = subDepartments; }
}
