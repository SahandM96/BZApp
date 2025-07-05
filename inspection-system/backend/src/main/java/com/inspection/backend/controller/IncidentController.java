package com.inspection.backend.controller;

import com.inspection.backend.dto.IncidentDto;
import com.inspection.backend.dto.IncidentRequestDto;
import com.inspection.backend.service.IncidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import com.inspection.backend.security.UserPrincipal; // Your custom UserPrincipal

import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    @Autowired
    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    public ResponseEntity<List<IncidentDto>> getAllIncidents(
            @RequestParam Optional<Long> departmentId,
            @RequestParam Optional<Long> reportedByUserId,
            @RequestParam Optional<String> status,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<Date> startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<Date> endDate) {
        List<IncidentDto> incidents;
        // This filtering logic could be more complex and potentially moved to the service layer
        if (startDate.isPresent() && endDate.isPresent()) {
            incidents = incidentService.findIncidentsByDateRange(startDate.get(), endDate.get());
        } else if (departmentId.isPresent()) {
            incidents = incidentService.findIncidentsByDepartmentId(departmentId.get());
        } else if (reportedByUserId.isPresent()) {
            incidents = incidentService.findIncidentsByReportedUserId(reportedByUserId.get());
        } else if (status.isPresent()) {
            incidents = incidentService.findIncidentsByStatus(status.get());
        } else {
            incidents = incidentService.findAllIncidents();
        }
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentDto> getIncidentById(@PathVariable Long id) {
        return incidentService.findIncidentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<?> createIncident(
            @Valid @RequestBody IncidentRequestDto incidentRequestDto
            /*, @AuthenticationPrincipal UserPrincipal currentUser */) {
        try {
            // Long reportedByUserId = currentUser.getId();
            Long reportedByUserId = 1L; // Placeholder
            IncidentDto createdIncident = incidentService.createIncident(incidentRequestDto, reportedByUserId);
            return new ResponseEntity<>(createdIncident, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // @PreAuthorize("hasRole('MANAGER') or @incidentSecurityService.isReporter(#id, principal.id)")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateIncident(
            @PathVariable Long id,
            @Valid @RequestBody IncidentRequestDto incidentRequestDto
            /*, @AuthenticationPrincipal UserPrincipal currentUser */) {
        try {
            // Long currentUserId = currentUser.getId(); // For potential ownership checks in service
            Long currentUserId = 1L; // Placeholder
            IncidentDto updatedIncident = incidentService.updateIncident(id, incidentRequestDto, currentUserId);
            return ResponseEntity.ok(updatedIncident);
        } catch (RuntimeException e) {
             if (e.getMessage().toLowerCase().contains("not found")) {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateIncidentStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String newStatus = payload.get("status");
         if (newStatus == null || newStatus.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "status is required."));
        }
        try {
            IncidentDto updatedIncident = incidentService.changeIncidentStatus(id, newStatus);
            return ResponseEntity.ok(updatedIncident);
        } catch (RuntimeException e) {
             if (e.getMessage().toLowerCase().contains("not found")) {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // @PreAuthorize("hasRole('ADMIN') or @incidentSecurityService.isReporter(#id, principal.id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteIncident(@PathVariable Long id) {
        try {
            incidentService.deleteIncident(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
