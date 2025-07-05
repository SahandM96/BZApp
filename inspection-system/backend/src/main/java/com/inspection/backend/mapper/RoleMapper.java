package com.inspection.backend.mapper;

import com.inspection.backend.entity.Role;
import com.inspection.backend.dto.RoleDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleMapper INSTANCE = Mappers.getMapper(RoleMapper.class);

    RoleDto roleToRoleDto(Role role);

    Role roleDtoToRole(RoleDto roleDto);

    List<RoleDto> rolesToRoleDtos(List<Role> roles);

    List<Role> roleDtosToRoles(List<RoleDto> roleDtos);
}
