package com.inspection.backend.repository;

import com.inspection.backend.entity.Process;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessRepository extends JpaRepository<Process, Long> {
    List<Process> findByDepartmentId(Long departmentId);
    List<Process> findByCreatedById(Long userId);
    List<Process> findByDepartmentIdAndStatus(Long departmentId, String status);
    List<Process> findByStatus(String status);
}
