package com.avanzada.alojamientos.mappers;

import com.avanzada.alojamientos.DTO.CreateCommentDTO;
import com.avanzada.alojamientos.DTO.CommentDTO;
import com.avanzada.alojamientos.entities.CommentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {

    // Entity -> DTO
    @Mapping(source = "reservation.id", target = "reservationId")
    @Mapping(source = "accommodation.id", target = "accommodationId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "text", target = "text")
    @Mapping(source = "hostReply", target = "hostReply")
    CommentDTO toCommentDTO(CommentEntity comment);



    // Create DTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "isModerated", constant = "false")
    @Mapping(target = "reservation", ignore = true) // Se asignará en el servicio
    @Mapping(target = "accommodation", ignore = true) // Se asignará en el servicio
    @Mapping(target = "user", ignore = true) // Se asignará en el servicio
    @Mapping(target = "hostReply", ignore = true)
    @Mapping(source = "text", target = "text")
    CommentEntity toEntity(CreateCommentDTO createCommentDTO);
}
