package com.inspection.backend.mapper;

import com.inspection.backend.entity.Process;
import com.inspection.backend.entity.Department;
import com.inspection.backend.entity.User;
import com.inspection.backend.dto.ProcessDto;
import com.inspection.backend.dto.ProcessRequestDto;
import com.inspection.backend.dto.GoalBaseDto; // Assuming GoalBaseDto exists
import com.inspection.backend.dto.KpiBaseDto;  // Assuming KpiBaseDto exists

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {GoalMapper.class, KpiMapper.class}) // For List<GoalBaseDto> and List<KpiBaseDto>
public interface ProcessMapper {

    ProcessMapper INSTANCE = Mappers.getMapper(ProcessMapper.class);

    @Mappings({
        @Mapping(source = "department.id", target = "departmentId"),
        @Mapping(source = "department.name", target = "departmentName"),
        @Mapping(source = "createdBy.id", target = "createdByUserId"),
        @Mapping(source = "createdBy.username", target = "createdByUserName"),
        @Mapping(source = "goals", target = "goals"), // Relies on GoalMapper providing Goal to GoalBaseDto
        @Mapping(source = "kpis", target = "kpis")   // Relies on KpiMapper providing KPI to KpiBaseDto
    })
    ProcessDto processToProcessDto(Process process);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "department", ignore = true), // Set from departmentId in service
        @Mapping(target = "createdBy", ignore = true),  // Set from authenticated user in service
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "kpis", ignore = true), // KPIs typically managed separately
        @Mapping(target = "goals", ignore = true)  // Goals typically managed separately
    })
    Process processRequestDtoToProcess(ProcessRequestDto processRequestDto);

    List<ProcessDto> processesToProcessDtos(List<Process> processes);

    // These named mappers are helpers and might not be strictly needed if direct field access works
    @Named("departmentToDepartmentId")
    default Long departmentToDepartmentId(Department department) {
        return department != null ? department.getId() : null;
    }

    @Named("userToUserId")
    default Long userToUserId(User user) {
        return user != null ? user.getId() : null;
    }
}
