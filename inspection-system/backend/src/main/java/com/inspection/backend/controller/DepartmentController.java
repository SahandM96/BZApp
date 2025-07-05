package com.inspection.backend.controller;

import com.inspection.backend.dto.DepartmentDto;
import com.inspection.backend.dto.DepartmentRequestDto;
import com.inspection.backend.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// import org.springframework.security.access.prepost.PreAuthorize;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    @Autowired
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public ResponseEntity<List<DepartmentDto>> getAllDepartments(@RequestParam Optional<Long> parentId) {
        List<DepartmentDto> departments;
        if (parentId.isPresent()) {
            departments = departmentService.findSubDepartments(parentId.get());
        } else {
            departments = departmentService.findAllDepartments();
        }
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDto> getDepartmentById(@PathVariable Long id) {
        return departmentService.findDepartmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<DepartmentDto> getDepartmentByName(@PathVariable String name) {
        return departmentService.findDepartmentByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')") // Example authorization
    @PostMapping
    public ResponseEntity<?> createDepartment(@Valid @RequestBody DepartmentRequestDto departmentRequestDto) {
        try {
            DepartmentDto createdDepartment = departmentService.createDepartment(departmentRequestDto);
            return new ResponseEntity<>(createdDepartment, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentRequestDto departmentRequestDto) {
        try {
            DepartmentDto updatedDepartment = departmentService.updateDepartment(id, departmentRequestDto);
            return ResponseEntity.ok(updatedDepartment);
        } catch (RuntimeException e) {
             if (e.getMessage().toLowerCase().contains("not found")) {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PutMapping("/{departmentId}/manager")
    public ResponseEntity<?> assignManager(
            @PathVariable Long departmentId,
            @RequestBody Map<String, Long> payload) { // Expecting {"managerUserId": 123}
        Long managerUserId = payload.get("managerUserId");
        if (managerUserId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "managerUserId is required."));
        }
        try {
            DepartmentDto updatedDepartment = departmentService.assignManagerToDepartment(departmentId, managerUserId);
            return ResponseEntity.ok(updatedDepartment);
        } catch (RuntimeException e) {
             if (e.getMessage().toLowerCase().contains("not found")) {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id) {
        try {
            departmentService.deleteDepartment(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            // More specific error messages can be returned
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
