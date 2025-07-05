package com.inspection.backend.mapper;

import com.inspection.backend.entity.Incident;
import com.inspection.backend.entity.Department;
import com.inspection.backend.entity.User;
import com.inspection.backend.dto.IncidentDto;
import com.inspection.backend.dto.IncidentRequestDto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IncidentMapper {

    IncidentMapper INSTANCE = Mappers.getMapper(IncidentMapper.class);

    @Mappings({
        @Mapping(source = "department.id", target = "departmentId"),
        @Mapping(source = "department.name", target = "departmentName"),
        @Mapping(source = "reportedByUser.id", target = "reportedByUserId"),
        @Mapping(source = "reportedByUser.username", target = "reportedByUserName")
    })
    IncidentDto incidentToIncidentDto(Incident incident);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "department", ignore = true),     // Set from departmentId in service
        @Mapping(target = "reportedByUser", ignore = true), // Set from authenticated user in service
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true)
    })
    Incident incidentRequestDtoToIncident(IncidentRequestDto incidentRequestDto);

    List<IncidentDto> incidentsToIncidentDtos(List<Incident> incidents);

    @Named("departmentToDepartmentId")
    default Long departmentToDepartmentId(Department department) {
        return department != null ? department.getId() : null;
    }

    @Named("userToUserId")
    default Long userToUserId(User user) {
        return user != null ? user.getId() : null;
    }
}
