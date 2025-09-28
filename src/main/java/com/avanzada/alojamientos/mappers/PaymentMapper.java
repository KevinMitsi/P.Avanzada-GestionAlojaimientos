package com.avanzada.alojamientos.mappers;

import com.avanzada.alojamientos.DTO.other.PaymentDTO;
import com.avanzada.alojamientos.entities.PaymentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {

    @Mapping(source = "id", target = "id", numberFormat = "#")
    @Mapping(source = "reservation.id", target = "reservationId", numberFormat = "#")
    @Mapping(source = "paidAt", target = "paidAt", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    PaymentDTO toDTO(PaymentEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reservation", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
    PaymentEntity toEntity(PaymentDTO dto);
}