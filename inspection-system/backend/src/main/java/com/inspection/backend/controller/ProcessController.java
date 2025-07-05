package com.inspection.backend.controller;

import com.inspection.backend.dto.ProcessDto;
import com.inspection.backend.dto.ProcessRequestDto;
import com.inspection.backend.service.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import com.inspection.backend.security.UserPrincipal; // Your custom UserPrincipal

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/processes")
public class ProcessController {

    private final ProcessService processService;

    @Autowired
    public ProcessController(ProcessService processService) {
        this.processService = processService;
    }

    @GetMapping
    public ResponseEntity<List<ProcessDto>> getAllProcesses(
            @RequestParam Optional<Long> departmentId,
            @RequestParam Optional<String> status) {
        List<ProcessDto> processes;
        if (departmentId.isPresent() && status.isPresent()) {
            processes = processService.findProcessesByDepartmentAndStatus(departmentId.get(), status.get());
        } else if (departmentId.isPresent()) {
            processes = processService.findProcessesByDepartmentId(departmentId.get());
        } else if (status.isPresent()) {
            processes = processService.findProcessesByStatus(status.get());
        } else {
            processes = processService.findAllProcesses();
        }
        return ResponseEntity.ok(processes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcessDto> getProcessById(@PathVariable Long id) {
        return processService.findProcessById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // @PreAuthorize("isAuthenticated()") // Example: User must be authenticated
    @PostMapping
    public ResponseEntity<?> createProcess(
            @Valid @RequestBody ProcessRequestDto processRequestDto
            /*, @AuthenticationPrincipal UserPrincipal currentUser */) {
        try {
            // Long creatorUserId = currentUser.getId(); // Get ID from authenticated principal
            Long creatorUserId = 1L; // Placeholder until Spring Security Principal is fully integrated
            ProcessDto createdProcess = processService.createProcess(processRequestDto, creatorUserId);
            return new ResponseEntity<>(createdProcess, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // @PreAuthorize("hasRole('MANAGER') or @processSecurityService.isOwner(#id, principal.id)") // Example
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProcess(@PathVariable Long id, @Valid @RequestBody ProcessRequestDto processRequestDto) {
        try {
            ProcessDto updatedProcess = processService.updateProcess(id, processRequestDto);
            return ResponseEntity.ok(updatedProcess);
        } catch (RuntimeException e) {
             if (e.getMessage().toLowerCase().contains("not found")) {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateProcessStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
         // Expecting a JSON like {"status": "NEW_STATUS"}
        String newStatus = payload.get("status");
        if (newStatus == null || newStatus.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "status is required in request body."));
        }
        try {
            ProcessDto updatedProcess = processService.changeProcessStatus(id, newStatus);
            return ResponseEntity.ok(updatedProcess);
        } catch (RuntimeException e) {
             if (e.getMessage().toLowerCase().contains("not found")) {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // @PreAuthorize("hasRole('ADMIN') or @processSecurityService.isOwner(#id, principal.id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProcess(@PathVariable Long id) {
        try {
            processService.deleteProcess(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
             if (e.getMessage().toLowerCase().contains("not found")) {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
