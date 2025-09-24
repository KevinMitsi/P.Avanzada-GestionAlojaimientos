package com.avanzada.alojamientos.mappers;

import com.avanzada.alojamientos.DTO.NotificationDTO;
import com.avanzada.alojamientos.entities.NotificationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NotificationMapper {

    @Mapping(source = "id", target = "id", numberFormat = "#")
    @Mapping(source = "user.id", target = "userId", numberFormat = "#")
    @Mapping(source = "read", target = "read")
    @Mapping(source = "createdAt", target = "createdAt", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    @Mapping(target = "metadata", expression = "java(parseMetadata(entity.getMetadata()))")
    NotificationDTO toDTO(NotificationEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", ignore = true) // Will be set in service
    @Mapping(target = "read", constant = "false")
    @Mapping(target = "metadata", expression = "java(serializeMetadata(dto.metadata()))")
    NotificationEntity toEntity(NotificationDTO dto);


}