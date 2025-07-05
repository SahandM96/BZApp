package com.inspection.backend.mapper;

import com.inspection.backend.entity.Goal;
import com.inspection.backend.entity.Process;
import com.inspection.backend.entity.User;
import com.inspection.backend.entity.Department;
import com.inspection.backend.dto.GoalDto;
import com.inspection.backend.dto.GoalBaseDto;
import com.inspection.backend.dto.GoalRequestDto;
import com.inspection.backend.dto.KpiBaseDto; // Assuming KpiBaseDto exists

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {KpiMapper.class}) // For List<KpiBaseDto>
public interface GoalMapper {

    GoalMapper INSTANCE = Mappers.getMapper(GoalMapper.class);

    @Mappings({
        @Mapping(source = "process.id", target = "processId"),
        @Mapping(source = "process.name", target = "processName"),
        @Mapping(source = "createdBy.id", target = "createdByUserId"),
        @Mapping(source = "createdBy.username", target = "createdByUserName"),
        @Mapping(source = "responsibleUser.id", target = "responsibleUserId"),
        @Mapping(source = "responsibleUser.username", target = "responsibleUserName"),
        @Mapping(source = "department.id", target = "departmentId"),
        @Mapping(source = "department.name", target = "departmentName"),
        @Mapping(source = "kpis", target = "kpis") // Relies on KpiMapper providing KPI to KpiBaseDto
    })
    GoalDto goalToGoalDto(Goal goal);

    GoalBaseDto goalToGoalBaseDto(Goal goal); // For use in ProcessDto or other aggregate DTOs

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "process", ignore = true),       // Set from processId in service
        @Mapping(target = "createdBy", ignore = true),    // Set from authenticated user in service
        @Mapping(target = "responsibleUser", ignore = true),// Set from responsibleUserId in service
        @Mapping(target = "department", ignore = true),   // Set from departmentId in service
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "kpis", ignore = true)          // KPIs managed separately
    })
    Goal goalRequestDtoToGoal(GoalRequestDto goalRequestDto);

    List<GoalDto> goalsToGoalDtos(List<Goal> goals);
    List<GoalBaseDto> goalsToGoalBaseDtos(List<Goal> goals);


    // Helper named mappers (can be omitted if direct field access is sufficient)
    @Named("processToProcessId")
    default Long processToProcessId(Process process) {
        return process != null ? process.getId() : null;
    }

    @Named("userToUserId")
    default Long userToUserId(User user) {
        return user != null ? user.getId() : null;
    }

    @Named("departmentToDepartmentId")
    default Long departmentToDepartmentId(Department department) {
        return department != null ? department.getId() : null;
    }
}
