package com.inspection.backend.repository;

import com.inspection.backend.entity.KPI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KPIRepository extends JpaRepository<KPI, Long> {
    // List<KPI> findByProcessId(Long processId); // Commented out as KPI is now primarily linked to Goal
    List<KPI> findByGoalId(Long goalId);
    List<KPI> findByResponsibleUserId(Long userId);
    List<KPI> findByGoalIdAndResponsibleUserId(Long goalId, Long userId);
}
