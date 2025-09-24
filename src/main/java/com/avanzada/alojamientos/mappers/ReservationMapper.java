package com.avanzada.alojamientos.mappers;

import com.avanzada.alojamientos.DTO.CreateReservationDTO;
import com.avanzada.alojamientos.DTO.ReservationDTO;
import com.avanzada.alojamientos.entities.ReservationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReservationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nights", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "motivoCancelacion", ignore = true)
    @Mapping(target = "cancelledBy", ignore = true)
    @Mapping(target = "accommodation", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "payment", ignore = true)
    @Mapping(target = "comment", ignore = true)
    @Mapping(target = "guests", ignore = true)
    ReservationEntity toEntity(CreateReservationDTO dto);

    @Mapping(source = "id", target = "id", numberFormat = "#")
    @Mapping(source = "accommodation.id", target = "accommodationId", numberFormat = "#")
    @Mapping(source = "user.id", target = "userId", numberFormat = "#")
    @Mapping(source = "accommodation.host.id", target = "hostId", numberFormat = "#")
    @Mapping(source = "createdAt", target = "createdAt", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    @Mapping(source = "updatedAt", target = "updatedAt", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    @Mapping(source = "cancelledAt", target = "canceladoAt", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    @Mapping(source = "motivoCancelacion", target = "motivoCancelacion")
    @Mapping(source = "cancelledBy", target = "canceladoPor")
    ReservationDTO toDTO(ReservationEntity entity);

}
