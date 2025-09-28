package com.avanzada.alojamientos.mappers;

import com.avanzada.alojamientos.DTO.city.CityDTO;
import com.avanzada.alojamientos.entities.CityEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CityMapper {

    @Mapping(source = "id", target = "id", numberFormat = "#")
    CityDTO toModel(CityEntity entity);

    @Mapping(target = "accommodations", ignore = true)
    CityEntity toEntity(CityDTO cityDTO);
}