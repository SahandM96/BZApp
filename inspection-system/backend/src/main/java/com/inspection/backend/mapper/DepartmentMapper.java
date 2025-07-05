package com.inspection.backend.mapper;

import com.inspection.backend.entity.Department;
import com.inspection.backend.entity.User;
import com.inspection.backend.dto.DepartmentDto;
import com.inspection.backend.dto.DepartmentRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring") // Not using UserMapper here to avoid circular deps in basic DTOs
public interface DepartmentMapper {

    DepartmentMapper INSTANCE = Mappers.getMapper(DepartmentMapper.class);

    @Mappings({
        @Mapping(source = "parent", target = "parentId", qualifiedByName = "departmentToDepartmentId"),
        @Mapping(source = "parent.name", target = "parentName"),
        @Mapping(source = "manager", target = "managerUserId", qualifiedByName = "userToUserId"),
        @Mapping(source = "manager.username", target = "managerUserName"),
        @Mapping(target = "numberOfUsers", expression = "java(department.getUsers() != null ? department.getUsers().size() : 0)"),
        @Mapping(target = "numberOfProcesses", expression = "java(department.getProcesses() != null ? department.getProcesses().size() : 0)")
    })
    DepartmentDto departmentToDepartmentDto(Department department);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "parent", ignore = true), // Will be set from parentId in service
        @Mapping(target = "manager", ignore = true), // Will be set from managerUserId in service
        @Mapping(target = "subDepartments", ignore = true),
        @Mapping(target = "users", ignore = true),
        @Mapping(target = "processes", ignore = true),
        @Mapping(target = "goals", ignore = true),
        @Mapping(target = "incidents", ignore = true)
    })
    Department departmentRequestDtoToDepartment(DepartmentRequestDto departmentRequestDto);

    List<DepartmentDto> departmentsToDepartmentDtos(List<Department> departments);

    @Named("departmentToDepartmentId")
    default Long departmentToDepartmentId(Department department) {
        return department != null ? department.getId() : null;
    }

    @Named("userToUserId")
    default Long userToUserId(User user) {
        return user != null ? user.getId() : null;
    }
}
