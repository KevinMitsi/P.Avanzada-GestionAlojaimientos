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
    @Mapping(source = "metadata", target = "metadata")
    NotificationDTO toDTO(NotificationEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "read", constant = "false")
    @Mapping(source = "metadata", target = "metadata")
    NotificationEntity toEntity(NotificationDTO dto);


}