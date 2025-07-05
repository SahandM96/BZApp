package com.inspection.backend.repository;

import com.inspection.backend.entity.KPI;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KPIRepository extends JpaRepository<KPI, Long> {
    List<KPI> findByProcessId(Long processId);
}
