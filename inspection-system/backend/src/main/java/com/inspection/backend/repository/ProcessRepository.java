package com.inspection.backend.repository;

import com.inspection.backend.entity.Process;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessRepository extends JpaRepository<Process, Long> {
    List<Process> findByDepartmentId(Long departmentId);
}
