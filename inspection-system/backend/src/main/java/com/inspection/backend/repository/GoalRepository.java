package com.inspection.backend.repository;

import com.inspection.backend.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByProcessId(Long processId);
    List<Goal> findByDepartmentId(Long departmentId);
    List<Goal> findByCreatedById(Long userId);
    List<Goal> findByResponsibleUserId(Long userId);
    List<Goal> findByStatus(String status);
    List<Goal> findByDepartmentIdAndStatus(Long departmentId, String status);
    List<Goal> findByProcessIdAndStatus(Long processId, String status);
}
