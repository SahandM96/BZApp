package com.inspection.backend.service;

import com.inspection.backend.entity.Process;
import com.inspection.backend.entity.User;
import com.inspection.backend.entity.Department;
import com.inspection.backend.dto.ProcessDto;
import com.inspection.backend.dto.ProcessRequestDto;
import com.inspection.backend.mapper.ProcessMapper;
import com.inspection.backend.repository.ProcessRepository;
import com.inspection.backend.repository.UserRepository;
import com.inspection.backend.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProcessService {

    private final ProcessRepository processRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final ProcessMapper processMapper;

    @Autowired
    public ProcessService(ProcessRepository processRepository, UserRepository userRepository,
                          DepartmentRepository departmentRepository, ProcessMapper processMapper) {
        this.processRepository = processRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.processMapper = processMapper;
    }

    @Transactional(readOnly = true)
    public List<ProcessDto> findAllProcesses() {
        return processRepository.findAll().stream()
                .map(processMapper::processToProcessDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ProcessDto> findProcessById(Long id) {
        return processRepository.findById(id).map(processMapper::processToProcessDto);
    }

    @Transactional(readOnly = true)
    public List<ProcessDto> findProcessesByDepartmentId(Long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new RuntimeException("Department not found with id: " + departmentId);
        }
        return processRepository.findByDepartmentId(departmentId).stream()
                .map(processMapper::processToProcessDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProcessDto> findProcessesByStatus(String status) {
        return processRepository.findByStatus(status).stream()
                .map(processMapper::processToProcessDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProcessDto> findProcessesByDepartmentAndStatus(Long departmentId, String status) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new RuntimeException("Department not found with id: " + departmentId);
        }
        return processRepository.findByDepartmentIdAndStatus(departmentId, status).stream()
                .map(processMapper::processToProcessDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProcessDto createProcess(ProcessRequestDto processRequestDto, Long creatorUserId) {
        Process process = processMapper.processRequestDtoToProcess(processRequestDto);

        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new RuntimeException("Creator User not found with id: " + creatorUserId));
        Department department = departmentRepository.findById(processRequestDto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + processRequestDto.getDepartmentId()));

        process.setCreatedBy(creator);
        process.setDepartment(department);
        if (process.getStatus() == null || process.getStatus().isEmpty()) {
            process.setStatus("DRAFT"); // Default status
        }
        Process savedProcess = processRepository.save(process);
        return processMapper.processToProcessDto(savedProcess);
    }

    @Transactional
    public ProcessDto updateProcess(Long id, ProcessRequestDto processRequestDto) {
        Process existingProcess = processRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Process not found with id: " + id));

        // Update fields from DTO
        existingProcess.setName(processRequestDto.getName());
        existingProcess.setDescription(processRequestDto.getDescription());
        existingProcess.setInputs(processRequestDto.getInputs());
        existingProcess.setOutputs(processRequestDto.getOutputs());

        if (processRequestDto.getStatus() != null) {
            existingProcess.setStatus(processRequestDto.getStatus());
        }

        if (processRequestDto.getDepartmentId() != null &&
            !processRequestDto.getDepartmentId().equals(existingProcess.getDepartment().getId())) {
            Department department = departmentRepository.findById(processRequestDto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found with id: " + processRequestDto.getDepartmentId()));
            existingProcess.setDepartment(department);
        }
        // createdBy should generally not be changed after creation

        Process updatedProcess = processRepository.save(existingProcess);
        return processMapper.processToProcessDto(updatedProcess);
    }

    @Transactional
    public ProcessDto changeProcessStatus(Long processId, String status) {
        Process process = processRepository.findById(processId)
            .orElseThrow(() -> new RuntimeException("Process not found with id: " + processId));
        // Add validation for allowed status transitions if necessary
        process.setStatus(status);
        Process updatedProcess = processRepository.save(process);
        return processMapper.processToProcessDto(updatedProcess);
    }

    @Transactional
    public void deleteProcess(Long id) {
        if (!processRepository.existsById(id)) {
            throw new RuntimeException("Process not found with id: " + id);
        }
        // Add checks for associated Goals/KPIs if deletion should be prevented
        processRepository.deleteById(id);
    }
}
