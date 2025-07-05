package com.inspection.backend.controller;

import com.inspection.backend.dto.GoalDto;
import com.inspection.backend.dto.GoalRequestDto;
import com.inspection.backend.service.GoalService;
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
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    @Autowired
    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @GetMapping
    public ResponseEntity<List<GoalDto>> getAllGoals(
            @RequestParam Optional<Long> processId,
            @RequestParam Optional<Long> departmentId,
            @RequestParam Optional<Long> responsibleUserId,
            @RequestParam Optional<String> status) { // Querying by status might need a dedicated service method or more complex logic
        List<GoalDto> goals;
        // This logic can be more sophisticated in the service layer for multiple filters
        if (processId.isPresent()) {
            goals = goalService.findGoalsByProcessId(processId.get());
        } else if (departmentId.isPresent()) {
            goals = goalService.findGoalsByDepartmentId(departmentId.get());
        } else if (responsibleUserId.isPresent()) {
            goals = goalService.findGoalsByResponsibleUserId(responsibleUserId.get());
        // } else if (status.isPresent()) {
            // goals = goalService.findGoalsByStatus(status.get()); // Requires service method
        } else {
            goals = goalService.findAllGoals();
        }
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalDto> getGoalById(@PathVariable Long id) {
        return goalService.findGoalById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<?> createGoal(
            @Valid @RequestBody GoalRequestDto goalRequestDto
            /*, @AuthenticationPrincipal UserPrincipal currentUser */) {
        try {
            // Long creatorUserId = currentUser.getId();
            Long creatorUserId = 1L; // Placeholder
            GoalDto createdGoal = goalService.createGoal(goalRequestDto, creatorUserId);
            return new ResponseEntity<>(createdGoal, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // @PreAuthorize("hasRole('MANAGER') or @goalSecurityService.isOwnerOrResponsible(#id, principal.id)")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGoal(@PathVariable Long id, @Valid @RequestBody GoalRequestDto goalRequestDto) {
        try {
            GoalDto updatedGoal = goalService.updateGoal(id, goalRequestDto);
            return ResponseEntity.ok(updatedGoal);
        } catch (RuntimeException e) {
             if (e.getMessage().toLowerCase().contains("not found")) {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // @PreAuthorize("hasRole('MANAGER') or @goalSecurityService.isOwnerOrResponsible(#id, principal.id)")
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateGoalStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String newStatus = payload.get("status");
        if (newStatus == null || newStatus.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "status is required."));
        }
        try {
            GoalDto updatedGoal = goalService.changeGoalStatus(id, newStatus);
            return ResponseEntity.ok(updatedGoal);
        } catch (RuntimeException e) {
             if (e.getMessage().toLowerCase().contains("not found")) {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // @PreAuthorize("hasRole('MANAGER') or @goalSecurityService.isOwner(#id, principal.id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGoal(@PathVariable Long id) {
        try {
            goalService.deleteGoal(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().toLowerCase().contains("not found")) {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
            // For "Cannot delete goal: it has associated KPIs"
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }
}
