package com.inspection.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "department")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"subDepartments", "users", "processes", "goals", "incidents", "parent", "manager"}) // Exclude collections and LAZY/EAGER direct relations
@ToString(exclude = {"subDepartments", "users", "processes", "goals", "incidents", "parent", "manager"}) // Exclude collections
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Department parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Department> subDepartments;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<User> users;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_user_id")
    private User manager;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<Process> processes;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<Goal> goals;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<Incident> incidents;
}
