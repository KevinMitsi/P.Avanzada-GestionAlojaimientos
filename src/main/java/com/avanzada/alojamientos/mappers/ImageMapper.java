package com.avanzada.alojamientos.mappers;

import com.avanzada.alojamientos.DTO.other.ImageDTO;
import com.avanzada.alojamientos.entities.ImageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ImageMapper {

    @Mapping(source = "id", target = "id", numberFormat = "#")
    @Mapping(source = "createdAt", target = "createdAt", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    ImageDTO toModel(ImageEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "accommodation", ignore = true)
    @Mapping(target = "isPrimary", constant = "false")
    ImageEntity toEntity(ImageDTO imageDTO);
}