package com.avanzada.alojamientos.mappers;

import com.avanzada.alojamientos.DTO.other.FavoriteDTO;
import com.avanzada.alojamientos.entities.FavoriteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {FavoriteAccommodationMapper.class})
public interface FavoriteMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "accommodation", target = "accommodation")
    @Mapping(source = "createdAt", target = "createdA", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    FavoriteDTO toDTO(FavoriteEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "accommodation", ignore = true)
    FavoriteEntity toEntity(FavoriteDTO dto);
}