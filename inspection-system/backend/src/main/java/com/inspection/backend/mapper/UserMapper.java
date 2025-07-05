package com.inspection.backend.mapper;

import com.inspection.backend.entity.User;
import com.inspection.backend.entity.Department;
import com.inspection.backend.entity.Role;
import com.inspection.backend.dto.UserDto;
import com.inspection.backend.dto.UserCreationRequestDto;
import com.inspection.backend.dto.UserUpdateRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {RoleMapper.class, DepartmentMapper.class})
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mappings({
        @Mapping(source = "department", target = "departmentId", qualifiedByName = "departmentToDepartmentId"),
        @Mapping(source = "department.name", target = "departmentName"),
        @Mapping(source = "role.name", target = "roleName")
    })
    UserDto userToUserDto(User user);

    // For UserCreationRequestDto, password will be handled in service
    // Role and Department will be fetched by ID in service
    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "passwordHash", source="password"), // Password will be hashed in service
        @Mapping(target = "department", ignore = true), // Will be set in service from departmentId
        @Mapping(target = "role", ignore = true), // Will be set in service from roleName
        @Mapping(target = "isActive", constant = "true"), // Default value
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdProcesses", ignore = true),
        @Mapping(target = "createdGoals", ignore = true),
        @Mapping(target = "responsibleForGoals", ignore = true),
        @Mapping(target = "responsibleForKpis", ignore = true),
        @Mapping(target = "reportedIncidents", ignore = true),
        @Mapping(target = "managedDepartments", ignore = true)
    })
    User userCreationRequestDtoToUser(UserCreationRequestDto userCreationRequestDto);

    // For UserUpdateRequestDto, handle partial updates in service.
    // MapStruct can be used to update an existing entity: void updateUserFromDto(UserUpdateRequestDto dto, @MappingTarget User entity);
    // However, careful handling of nulls and associations is needed in service layer.
    // For simplicity, we can map to a new User object and then selectively update fields in the service.
    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "passwordHash", ignore = true), // Password update handled separately
        @Mapping(target = "department", ignore = true),
        @Mapping(target = "role", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdProcesses", ignore = true),
        @Mapping(target = "createdGoals", ignore = true),
        @Mapping(target = "responsibleForGoals", ignore = true),
        @Mapping(target = "responsibleForKpis", ignore = true),
        @Mapping(target = "reportedIncidents", ignore = true),
        @Mapping(target = "managedDepartments", ignore = true)
    })
    User userUpdateRequestDtoToUser(UserUpdateRequestDto userUpdateRequestDto);


    List<UserDto> usersToUserDtos(List<User> users);

    @Named("departmentToDepartmentId")
    default Long departmentToDepartmentId(Department department) {
        return department != null ? department.getId() : null;
    }
}
