package com.inspection.backend.controller;

import com.inspection.backend.dto.KpiDto;
import com.inspection.backend.dto.KpiRequestDto;
import com.inspection.backend.service.KpiService;
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
@RequestMapping("/api/kpis")
public class KpiController {

    private final KpiService kpiService;

    @Autowired
    public KpiController(KpiService kpiService) {
        this.kpiService = kpiService;
    }

    @GetMapping
    public ResponseEntity<List<KpiDto>> getAllKpis(
            @RequestParam Optional<Long> goalId,
            @RequestParam Optional<Long> responsibleUserId) {
        List<KpiDto> kpis;
        if (goalId.isPresent()) {
            kpis = kpiService.findKpisByGoalId(goalId.get());
        } else if (responsibleUserId.isPresent()) {
            kpis = kpiService.findKpisByResponsibleUserId(responsibleUserId.get());
        } else {
            kpis = kpiService.findAllKpis();
        }
        return ResponseEntity.ok(kpis);
    }

    @GetMapping("/{id}")
    public ResponseEntity<KpiDto> getKpiById(@PathVariable Long id) {
        return kpiService.findKpiById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<?> createKpi(@Valid @RequestBody KpiRequestDto kpiRequestDto) {
        try {
            KpiDto createdKpi = kpiService.createKpi(kpiRequestDto);
            return new ResponseEntity<>(createdKpi, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // @PreAuthorize("hasRole('MANAGER') or @kpiSecurityService.isOwnerOrResponsible(#id, principal.id)")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateKpi(@PathVariable Long id, @Valid @RequestBody KpiRequestDto kpiRequestDto) {
        try {
            KpiDto updatedKpi = kpiService.updateKpi(id, kpiRequestDto);
            return ResponseEntity.ok(updatedKpi);
        } catch (RuntimeException e) {
             if (e.getMessage().toLowerCase().contains("not found")) {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // @PreAuthorize("isAuthenticated()") // Or more specific permission
    @PutMapping("/{id}/value")
    public ResponseEntity<?> updateKpiValue(@PathVariable Long id, @RequestBody Map<String, Double> payload) {
        // Expecting {"actualValue": 123.45} or {"actualValue": null}
        Double actualValue = payload.get("actualValue");
        // No specific check for null here, service layer handles Double
        try {
            KpiDto updatedKpi = kpiService.updateKpiValue(id, actualValue);
            return ResponseEntity.ok(updatedKpi);
        } catch (RuntimeException e) {
             if (e.getMessage().toLowerCase().contains("not found")) {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // @PreAuthorize("hasRole('MANAGER') or @kpiSecurityService.isOwnerOrResponsible(#id, principal.id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteKpi(@PathVariable Long id) {
        try {
            kpiService.deleteKpi(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
