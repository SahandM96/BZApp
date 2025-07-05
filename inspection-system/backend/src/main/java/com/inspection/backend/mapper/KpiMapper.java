package com.inspection.backend.mapper;

import com.inspection.backend.entity.KPI;
import com.inspection.backend.entity.Goal;
import com.inspection.backend.entity.User;
import com.inspection.backend.dto.KpiDto;
import com.inspection.backend.dto.KpiBaseDto;
import com.inspection.backend.dto.KpiRequestDto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface KpiMapper {

    KpiMapper INSTANCE = Mappers.getMapper(KpiMapper.class);

    @Mappings({
        @Mapping(source = "goal.id", target = "goalId"),
        @Mapping(source = "goal.name", target = "goalName"),
        @Mapping(source = "responsibleUser.id", target = "responsibleUserId"),
        @Mapping(source = "responsibleUser.username", target = "responsibleUserName")
    })
    KpiDto kpiToKpiDto(KPI kpi);

    KpiBaseDto kpiToKpiBaseDto(KPI kpi); // For use in ProcessDto/GoalDto

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "goal", ignore = true),             // Set from goalId in service
        @Mapping(target = "responsibleUser", ignore = true), // Set from responsibleUserId in service
        @Mapping(target = "lastUpdatedValueDate", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true)
    })
    KPI kpiRequestDtoToKpi(KpiRequestDto kpiRequestDto);

    List<KpiDto> kpisToKpiDtos(List<KPI> kpis);
    List<KpiBaseDto> kpisToKpiBaseDtos(List<KPI> kpis);


    @Named("goalToGoalId")
    default Long goalToGoalId(Goal goal) {
        return goal != null ? goal.getId() : null;
    }

    @Named("userToUserId")
    default Long userToUserId(User user) {
        return user != null ? user.getId() : null;
    }
}
