package com.inspection.backend.service;

import com.inspection.backend.entity.Department;
import com.inspection.backend.entity.User;
import com.inspection.backend.dto.DepartmentDto;
import com.inspection.backend.dto.DepartmentRequestDto;
import com.inspection.backend.mapper.DepartmentMapper;
import com.inspection.backend.repository.DepartmentRepository;
import com.inspection.backend.repository.UserRepository;
import com.inspection.backend.repository.ProcessRepository; // For checking associated processes
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final ProcessRepository processRepository; // Added for cascading checks
    private final DepartmentMapper departmentMapper;

    @Autowired
    public DepartmentService(DepartmentRepository departmentRepository, UserRepository userRepository,
                             ProcessRepository processRepository, DepartmentMapper departmentMapper) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.processRepository = processRepository;
        this.departmentMapper = departmentMapper;
    }

    @Transactional(readOnly = true)
    public List<DepartmentDto> findAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(departmentMapper::departmentToDepartmentDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<DepartmentDto> findDepartmentById(Long id) {
        return departmentRepository.findById(id).map(departmentMapper::departmentToDepartmentDto);
    }

    @Transactional(readOnly = true)
    public Optional<DepartmentDto> findDepartmentByName(String name) {
        return departmentRepository.findByName(name).map(departmentMapper::departmentToDepartmentDto);
    }

    @Transactional(readOnly = true)
    public List<DepartmentDto> findSubDepartments(Long parentId) {
        if (!departmentRepository.existsById(parentId)) {
            throw new RuntimeException("Parent department not found with id: " + parentId);
        }
        return departmentRepository.findByParentId(parentId).stream()
                .map(departmentMapper::departmentToDepartmentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DepartmentDto createDepartment(DepartmentRequestDto departmentRequestDto) {
        Department department = departmentMapper.departmentRequestDtoToDepartment(departmentRequestDto);

        if (departmentRequestDto.getParentId() != null) {
            Department parentDepartment = departmentRepository.findById(departmentRequestDto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent department not found with id: " + departmentRequestDto.getParentId()));
            department.setParent(parentDepartment);
        }
        if (departmentRequestDto.getManagerUserId() != null) {
            User manager = userRepository.findById(departmentRequestDto.getManagerUserId())
                    .orElseThrow(() -> new RuntimeException("Manager user not found with id: " + departmentRequestDto.getManagerUserId()));
            // Optionally check manager role here
            department.setManager(manager);
        }
        Department savedDepartment = departmentRepository.save(department);
        return departmentMapper.departmentToDepartmentDto(savedDepartment);
    }

    @Transactional
    public DepartmentDto updateDepartment(Long id, DepartmentRequestDto departmentRequestDto) {
        Department existingDepartment = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));

        existingDepartment.setName(departmentRequestDto.getName());

        if (departmentRequestDto.getParentId() != null) {
            if (departmentRequestDto.getParentId().equals(id)) {
                throw new RuntimeException("Department cannot be its own parent.");
            }
            Department parentDepartment = departmentRepository.findById(departmentRequestDto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent department not found with id: " + departmentRequestDto.getParentId()));
            existingDepartment.setParent(parentDepartment);
        } else {
            existingDepartment.setParent(null);
        }

        if (departmentRequestDto.getManagerUserId() != null) {
            User manager = userRepository.findById(departmentRequestDto.getManagerUserId())
                    .orElseThrow(() -> new RuntimeException("Manager user not found with id: " + departmentRequestDto.getManagerUserId()));
            existingDepartment.setManager(manager);
        } else {
            existingDepartment.setManager(null);
        }

        Department updatedDepartment = departmentRepository.save(existingDepartment);
        return departmentMapper.departmentToDepartmentDto(updatedDepartment);
    }

    @Transactional
    public DepartmentDto assignManagerToDepartment(Long departmentId, Long managerUserId) {
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new RuntimeException("Department not found with id: " + departmentId));
        User manager = userRepository.findById(managerUserId)
            .orElseThrow(() -> new RuntimeException("Manager user not found with id: " + managerUserId));

        // Example role check (requires Role entity to be loaded or Role name in User)
        // if (!manager.getRole().getName().equals("MANAGER") && !manager.getRole().getName().equals("ADMIN")) {
        //    throw new RuntimeException("User does not have privileges to be a manager.");
        // }

        department.setManager(manager);
        Department savedDepartment = departmentRepository.save(department);
        return departmentMapper.departmentToDepartmentDto(savedDepartment);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));

        if (!departmentRepository.findByParentId(id).isEmpty()) {
            throw new RuntimeException("Cannot delete department: it has sub-departments. Please re-assign or delete them first.");
        }

        // Set department_id to null for associated users
        department.getUsers().forEach(user -> {
            user.setDepartment(null);
            userRepository.save(user);
        });

        // Set department_id to null for associated processes (or handle as per business logic)
        // This depends on the desired behavior. If processes MUST have a department, then deletion should be blocked.
        // Or, processes could be cascaded deleted if ON DELETE CASCADE is set in DB and JPA mapping.
        // For now, let's assume we block deletion if processes exist.
        if (!processRepository.findByDepartmentId(id).isEmpty()) {
            throw new RuntimeException("Cannot delete department: it has associated processes. Please re-assign or delete them first.");
        }
        // Similar checks for Goals, Incidents if necessary, depending on ON DELETE constraints.

        departmentRepository.deleteById(id);
    }
}
