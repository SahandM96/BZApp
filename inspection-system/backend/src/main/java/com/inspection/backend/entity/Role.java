package com.inspection.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "roles") // Explicitly mapping to "roles" table
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"users"}) // Avoid circular dependency in generated methods
@ToString(exclude = {"users"}) // Avoid circular dependency in generated methods
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g., "ADMIN", "MANAGER", "USER"

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY) // Added LAZY fetch type
    private List<User> users;
}
