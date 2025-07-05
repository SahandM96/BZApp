package com.inspection.backend.service;

import com.inspection.backend.entity.Goal;
import com.inspection.backend.entity.User;
import com.inspection.backend.entity.Process;
import com.inspection.backend.entity.Department;
import com.inspection.backend.dto.GoalDto;
import com.inspection.backend.dto.GoalRequestDto;
import com.inspection.backend.mapper.GoalMapper;
import com.inspection.backend.repository.GoalRepository;
import com.inspection.backend.repository.UserRepository;
import com.inspection.backend.repository.ProcessRepository;
import com.inspection.backend.repository.DepartmentRepository;
import com.inspection.backend.repository.KPIRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final ProcessRepository processRepository;
    private final DepartmentRepository departmentRepository;
    private final KPIRepository kpiRepository; // For checking associated KPIs
    private final GoalMapper goalMapper;

    @Autowired
    public GoalService(GoalRepository goalRepository, UserRepository userRepository,
                       ProcessRepository processRepository, DepartmentRepository departmentRepository,
                       KPIRepository kpiRepository, GoalMapper goalMapper) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
        this.processRepository = processRepository;
        this.departmentRepository = departmentRepository;
        this.kpiRepository = kpiRepository;
        this.goalMapper = goalMapper;
    }

    @Transactional(readOnly = true)
    public List<GoalDto> findAllGoals() {
        return goalRepository.findAll().stream()
                .map(goalMapper::goalToGoalDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<GoalDto> findGoalById(Long id) {
        return goalRepository.findById(id).map(goalMapper::goalToGoalDto);
    }

    @Transactional(readOnly = true)
    public List<GoalDto> findGoalsByProcessId(Long processId) {
        if (processId != null && !processRepository.existsById(processId)) {
            // Allow searching for goals not linked to any process if processId is null
            // Or throw if processId is mandatory for this search type
        }
        return goalRepository.findByProcessId(processId).stream()
                .map(goalMapper::goalToGoalDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GoalDto> findGoalsByDepartmentId(Long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new RuntimeException("Department not found with id: " + departmentId);
        }
        return goalRepository.findByDepartmentId(departmentId).stream()
                .map(goalMapper::goalToGoalDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GoalDto> findGoalsByResponsibleUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        return goalRepository.findByResponsibleUserId(userId).stream()
                .map(goalMapper::goalToGoalDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public GoalDto createGoal(GoalRequestDto goalRequestDto, Long creatorUserId) {
        Goal goal = goalMapper.goalRequestDtoToGoal(goalRequestDto);

        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new RuntimeException("Creator User not found with id: " + creatorUserId));
        goal.setCreatedBy(creator);

        if (goalRequestDto.getResponsibleUserId() != null) {
            User responsibleUser = userRepository.findById(goalRequestDto.getResponsibleUserId())
                    .orElseThrow(() -> new RuntimeException("Responsible User not found with id: " + goalRequestDto.getResponsibleUserId()));
            goal.setResponsibleUser(responsibleUser);
        }

        if (goalRequestDto.getProcessId() != null) {
            Process process = processRepository.findById(goalRequestDto.getProcessId())
                    .orElseThrow(() -> new RuntimeException("Process not found with id: " + goalRequestDto.getProcessId()));
            goal.setProcess(process);
            // If departmentId is not provided in DTO, infer from process
            if (goalRequestDto.getDepartmentId() == null && process.getDepartment() != null) {
                goal.setDepartment(process.getDepartment());
            }
        }

        if (goalRequestDto.getDepartmentId() != null) {
             Department department = departmentRepository.findById(goalRequestDto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + goalRequestDto.getDepartmentId()));
            goal.setDepartment(department);
        }

        if (goal.getDepartment() == null && goal.getProcess() == null) {
            // This condition might be too strict if a goal can be standalone at a higher organizational level
            // For now, let's assume it needs at least one association.
             throw new RuntimeException("Goal must be associated with a Department or a Process.");
        }

        if (goal.getStatus() == null || goal.getStatus().isEmpty()) {
            goal.setStatus("ACTIVE"); // Default status
        }
        Goal savedGoal = goalRepository.save(goal);
        return goalMapper.goalToGoalDto(savedGoal);
    }

    @Transactional
    public GoalDto updateGoal(Long id, GoalRequestDto goalRequestDto) {
        Goal existingGoal = goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found with id: " + id));

        // Update fields from DTO
        existingGoal.setName(goalRequestDto.getName());
        existingGoal.setDescription(goalRequestDto.getDescription());
        existingGoal.setTargetValue(goalRequestDto.getTargetValue());
        existingGoal.setTargetMetricDescription(goalRequestDto.getTargetMetricDescription());
        existingGoal.setStartDate(goalRequestDto.getStartDate());
        existingGoal.setEndDate(goalRequestDto.getEndDate());

        if(goalRequestDto.getStatus() != null) {
            existingGoal.setStatus(goalRequestDto.getStatus());
        }

        if (goalRequestDto.getResponsibleUserId() != null) {
            User responsibleUser = userRepository.findById(goalRequestDto.getResponsibleUserId())
                    .orElseThrow(() -> new RuntimeException("Responsible User not found with id: " + goalRequestDto.getResponsibleUserId()));
            existingGoal.setResponsibleUser(responsibleUser);
        } else { // Allow unsetting responsible user
            existingGoal.setResponsibleUser(null);
        }

        if (goalRequestDto.getProcessId() != null) {
            Process process = processRepository.findById(goalRequestDto.getProcessId())
                    .orElseThrow(() -> new RuntimeException("Process not found with id: " + goalRequestDto.getProcessId()));
            existingGoal.setProcess(process);
            // If department is not explicitly set in DTO, update it from new process
            if (goalRequestDto.getDepartmentId() == null && process.getDepartment() != null) {
                 existingGoal.setDepartment(process.getDepartment());
            }
        } else { // Allow unsetting process
            existingGoal.setProcess(null);
        }

        if (goalRequestDto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(goalRequestDto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found with id: " + goalRequestDto.getDepartmentId()));
            existingGoal.setDepartment(department);
        } else if (existingGoal.getProcess() == null) {
            // If process is also null, allow unsetting department.
            // If process is not null, department is usually inferred or already set.
            existingGoal.setDepartment(null);
        }


        Goal updatedGoal = goalRepository.save(existingGoal);
        return goalMapper.goalToGoalDto(updatedGoal);
    }

    @Transactional
    public GoalDto changeGoalStatus(Long goalId, String status) {
        Goal goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new RuntimeException("Goal not found with id: " + goalId));
        // Add validation for status values if necessary
        goal.setStatus(status);
        Goal updatedGoal = goalRepository.save(goal);
        return goalMapper.goalToGoalDto(updatedGoal);
    }

    @Transactional
    public void deleteGoal(Long id) {
        if (!goalRepository.existsById(id)) {
            throw new RuntimeException("Goal not found with id: " + id);
        }
        // Prevent deletion if KPIs are associated
        if (!kpiRepository.findByGoalId(id).isEmpty()) {
            throw new RuntimeException("Cannot delete goal: it has associated KPIs. Please delete them first.");
        }
        goalRepository.deleteById(id);
    }
}
