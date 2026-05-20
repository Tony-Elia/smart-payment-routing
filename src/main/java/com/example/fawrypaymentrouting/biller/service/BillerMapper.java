package com.example.fawrypaymentrouting.biller.service;

import com.example.fawrypaymentrouting.biller.dto.BillerRequestDTO;
import com.example.fawrypaymentrouting.biller.dto.BillerResponseDTO;
import com.example.fawrypaymentrouting.biller.model.Biller;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BillerMapper {

    BillerResponseDTO toDto(Biller biller);

    @Mapping(target = "id", ignore = true)
    Biller toEntity(BillerRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(BillerRequestDTO dto, @MappingTarget Biller biller);
}
