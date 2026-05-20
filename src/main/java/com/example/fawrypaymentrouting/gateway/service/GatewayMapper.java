package com.example.fawrypaymentrouting.gateway.service;

import com.example.fawrypaymentrouting.gateway.dto.GatewayRequestDto;
import com.example.fawrypaymentrouting.gateway.dto.GatewayResponseDto;
import com.example.fawrypaymentrouting.gateway.model.Gateway;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT
)
public interface GatewayMapper {

    GatewayResponseDto toDto(Gateway gateway);

    @Mapping(target = "id", ignore = true)
    Gateway toEntity(GatewayRequestDto dto);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(GatewayRequestDto dto, @MappingTarget Gateway gateway);
}
