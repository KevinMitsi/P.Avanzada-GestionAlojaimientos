package com.avanzada.alojamientos.mappers;

import com.avanzada.alojamientos.DTO.other.FavoriteAccommodationDTO;
import com.avanzada.alojamientos.entities.AccommodationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {CityMapper.class, ImageMapper.class})
public interface FavoriteAccommodationMapper {

    FavoriteAccommodationDTO toDTO(AccommodationEntity entity);
}
