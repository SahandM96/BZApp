package com.inspection.backend.repository;

import com.inspection.backend.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByDepartmentId(Long departmentId);
    List<Incident> findByReportedByUserId(Long userId);
    List<Incident> findByStatus(String status);
    List<Incident> findBySeverity(String severity);
    List<Incident> findByIncidentDateBetween(Date startDate, Date endDate);
    List<Incident> findByDepartmentIdAndStatus(Long departmentId, String status);
}
