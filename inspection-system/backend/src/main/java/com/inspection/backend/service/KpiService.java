package com.inspection.backend.service;

import com.inspection.backend.entity.KPI;
import com.inspection.backend.entity.Goal;
import com.inspection.backend.entity.User;
import com.inspection.backend.dto.KpiDto;
import com.inspection.backend.dto.KpiRequestDto;
import com.inspection.backend.mapper.KpiMapper;
import com.inspection.backend.repository.KPIRepository;
import com.inspection.backend.repository.GoalRepository;
import com.inspection.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class KpiService {

    private final KPIRepository kpiRepository;
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final KpiMapper kpiMapper;

    @Autowired
    public KpiService(KPIRepository kpiRepository, GoalRepository goalRepository,
                      UserRepository userRepository, KpiMapper kpiMapper) {
        this.kpiRepository = kpiRepository;
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
        this.kpiMapper = kpiMapper;
    }

    @Transactional(readOnly = true)
    public List<KpiDto> findAllKpis() {
        return kpiRepository.findAll().stream()
                .map(kpiMapper::kpiToKpiDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<KpiDto> findKpiById(Long id) {
        return kpiRepository.findById(id).map(kpiMapper::kpiToKpiDto);
    }

    @Transactional(readOnly = true)
    public List<KpiDto> findKpisByGoalId(Long goalId) {
        if (!goalRepository.existsById(goalId)) {
            throw new RuntimeException("Goal not found with id: " + goalId);
        }
        return kpiRepository.findByGoalId(goalId).stream()
                .map(kpiMapper::kpiToKpiDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<KpiDto> findKpisByResponsibleUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        return kpiRepository.findByResponsibleUserId(userId).stream()
                .map(kpiMapper::kpiToKpiDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public KpiDto createKpi(KpiRequestDto kpiRequestDto) {
        KPI kpi = kpiMapper.kpiRequestDtoToKpi(kpiRequestDto);

        Goal goal = goalRepository.findById(kpiRequestDto.getGoalId())
                .orElseThrow(() -> new RuntimeException("Goal not found with id: " + kpiRequestDto.getGoalId()));
        kpi.setGoal(goal);

        if (kpiRequestDto.getResponsibleUserId() != null) {
            User responsibleUser = userRepository.findById(kpiRequestDto.getResponsibleUserId())
                    .orElseThrow(() -> new RuntimeException("Responsible User not found with id: " + kpiRequestDto.getResponsibleUserId()));
            kpi.setResponsibleUser(responsibleUser);
        }
        // actualValue is typically set via updateKpiValue, so lastUpdatedValueDate set there
        KPI savedKpi = kpiRepository.save(kpi);
        return kpiMapper.kpiToKpiDto(savedKpi);
    }

    @Transactional
    public KpiDto updateKpi(Long id, KpiRequestDto kpiRequestDto) {
        KPI existingKpi = kpiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KPI not found with id: " + id));

        existingKpi.setName(kpiRequestDto.getName());
        existingKpi.setDescription(kpiRequestDto.getDescription());
        existingKpi.setTargetValue(kpiRequestDto.getTargetValue());
        // existingKpi.setActualValue(kpiRequestDto.getActualValue()); // Actual value updated via dedicated endpoint
        existingKpi.setUnit(kpiRequestDto.getUnit());
        existingKpi.setMeasurementFrequency(kpiRequestDto.getMeasurementFrequency());

        // If actualValue is part of this DTO and changes, update timestamp
        // For now, assuming actualValue is not updated here directly.

        if (kpiRequestDto.getResponsibleUserId() != null) {
            User responsibleUser = userRepository.findById(kpiRequestDto.getResponsibleUserId())
                    .orElseThrow(() -> new RuntimeException("Responsible User not found with id: " + kpiRequestDto.getResponsibleUserId()));
            existingKpi.setResponsibleUser(responsibleUser);
        } else {
            existingKpi.setResponsibleUser(null);
        }

        if (kpiRequestDto.getGoalId() != null && !kpiRequestDto.getGoalId().equals(existingKpi.getGoal().getId())) {
             Goal goal = goalRepository.findById(kpiRequestDto.getGoalId())
                .orElseThrow(() -> new RuntimeException("Goal not found with id: " + kpiRequestDto.getGoalId()));
            existingKpi.setGoal(goal);
        }

        KPI updatedKpi = kpiRepository.save(existingKpi);
        return kpiMapper.kpiToKpiDto(updatedKpi);
    }

    @Transactional
    public KpiDto updateKpiValue(Long kpiId, Double actualValue) { // Changed to Double to allow null
        KPI kpi = kpiRepository.findById(kpiId)
            .orElseThrow(() -> new RuntimeException("KPI not found with id: " + kpiId));
        kpi.setActualValue(actualValue);
        kpi.setLastUpdatedValueDate(Instant.now());
        KPI updatedKpi = kpiRepository.save(kpi);
        return kpiMapper.kpiToKpiDto(updatedKpi);
    }

    @Transactional
    public void deleteKpi(Long id) {
        if (!kpiRepository.existsById(id)) {
            throw new RuntimeException("KPI not found with id: " + id);
        }
        kpiRepository.deleteById(id);
    }
}
