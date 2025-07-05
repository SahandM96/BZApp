package com.inspection.backend.service;

import com.inspection.backend.entity.Incident;
import com.inspection.backend.entity.User;
import com.inspection.backend.entity.Department;
import com.inspection.backend.dto.IncidentDto;
import com.inspection.backend.dto.IncidentRequestDto;
import com.inspection.backend.mapper.IncidentMapper;
import com.inspection.backend.repository.IncidentRepository;
import com.inspection.backend.repository.UserRepository;
import com.inspection.backend.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Date;
import java.util.stream.Collectors;


@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final IncidentMapper incidentMapper;

    @Autowired
    public IncidentService(IncidentRepository incidentRepository, UserRepository userRepository,
                           DepartmentRepository departmentRepository, IncidentMapper incidentMapper) {
        this.incidentRepository = incidentRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.incidentMapper = incidentMapper;
    }

    @Transactional(readOnly = true)
    public List<IncidentDto> findAllIncidents() {
        return incidentRepository.findAll().stream()
                .map(incidentMapper::incidentToIncidentDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<IncidentDto> findIncidentById(Long id) {
        return incidentRepository.findById(id).map(incidentMapper::incidentToIncidentDto);
    }

    @Transactional(readOnly = true)
    public List<IncidentDto> findIncidentsByDepartmentId(Long departmentId) {
        if (departmentId != null && !departmentRepository.existsById(departmentId)) {
            // Or return empty list if departmentId is optional for this search
            throw new RuntimeException("Department not found with id: " + departmentId);
        }
        return incidentRepository.findByDepartmentId(departmentId).stream()
                .map(incidentMapper::incidentToIncidentDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<IncidentDto> findIncidentsByReportedUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        return incidentRepository.findByReportedByUserId(userId).stream()
                .map(incidentMapper::incidentToIncidentDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<IncidentDto> findIncidentsByStatus(String status) {
        return incidentRepository.findByStatus(status).stream()
                .map(incidentMapper::incidentToIncidentDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<IncidentDto> findIncidentsByDateRange(Date startDate, Date endDate) {
        return incidentRepository.findByIncidentDateBetween(startDate, endDate).stream()
                .map(incidentMapper::incidentToIncidentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public IncidentDto createIncident(IncidentRequestDto incidentRequestDto, Long reportedByUserId) {
        Incident incident = incidentMapper.incidentRequestDtoToIncident(incidentRequestDto);

        if (reportedByUserId != null) { // Should always be present if coming from authenticated user
            User reportedByUser = userRepository.findById(reportedByUserId)
                    .orElseThrow(() -> new RuntimeException("Reported-by User not found with id: " + reportedByUserId));
            incident.setReportedByUser(reportedByUser);
        }

        if (incidentRequestDto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(incidentRequestDto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found with id: " + incidentRequestDto.getDepartmentId()));
            incident.setDepartment(department);
        }

        if (incident.getStatus() == null || incident.getStatus().isEmpty()) {
            incident.setStatus("OPEN"); // Default status
        }
        // Incident date is set from DTO, which has @NotNull
        // if (incident.getIncidentDate() == null) {
        //     incident.setIncidentDate(new Date()); // Default to now if not provided
        // }

        Incident savedIncident = incidentRepository.save(incident);
        return incidentMapper.incidentToIncidentDto(savedIncident);
    }

    @Transactional
    public IncidentDto updateIncident(Long id, IncidentRequestDto incidentRequestDto, Long currentUserId) {
        Incident existingIncident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found with id: " + id));

        existingIncident.setDescription(incidentRequestDto.getDescription());
        existingIncident.setIncidentDate(incidentRequestDto.getIncidentDate());
        existingIncident.setSeverity(incidentRequestDto.getSeverity());

        if(incidentRequestDto.getStatus() != null){
            existingIncident.setStatus(incidentRequestDto.getStatus());
        }

        // reportedByUserId might not be updatable, or only by admin.
        // For now, let's assume it's not updated via this DTO after creation.
        // If it needs to be, add reportedByUserId to IncidentRequestDto and handle here.

        if (incidentRequestDto.getDepartmentId() != null) {
            if(existingIncident.getDepartment() == null || !incidentRequestDto.getDepartmentId().equals(existingIncident.getDepartment().getId())) {
                Department department = departmentRepository.findById(incidentRequestDto.getDepartmentId())
                        .orElseThrow(() -> new RuntimeException("Department not found with id: " + incidentRequestDto.getDepartmentId()));
                existingIncident.setDepartment(department);
            }
        } else { // Allow unsetting department
            existingIncident.setDepartment(null);
        }

        Incident updatedIncident = incidentRepository.save(existingIncident);
        return incidentMapper.incidentToIncidentDto(updatedIncident);
    }

    @Transactional
    public IncidentDto changeIncidentStatus(Long incidentId, String status) {
        Incident incident = incidentRepository.findById(incidentId)
            .orElseThrow(() -> new RuntimeException("Incident not found with id: " + incidentId));
        // Add validation for status values if necessary
        incident.setStatus(status);
        Incident updatedIncident = incidentRepository.save(incident);
        return incidentMapper.incidentToIncidentDto(updatedIncident);
    }

    @Transactional
    public void deleteIncident(Long id) {
        if (!incidentRepository.existsById(id)) {
            throw new RuntimeException("Incident not found with id: " + id);
        }
        incidentRepository.deleteById(id);
    }
}
