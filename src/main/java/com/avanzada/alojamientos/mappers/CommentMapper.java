package com.avanzada.alojamientos.mappers;

import com.avanzada.alojamientos.DTO.comment.CreateCommentDTO;
import com.avanzada.alojamientos.DTO.comment.CommentDTO;
import com.avanzada.alojamientos.DTO.host.ReplyHostDTO;
import com.avanzada.alojamientos.DTO.model.HostReply;
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
    @Mapping(source = "rating", target = "rating")
    @Mapping(source = "text", target = "text")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "isModerated", target = "isModerated")
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
    @Mapping(source = "rating", target = "rating")
    CommentEntity toEntity(CreateCommentDTO createCommentDTO);

    // Mapear HostReply a ReplyHostDTO
    @Mapping(source = "hostId", target = "hostId")
    @Mapping(source = "reply", target = "text")
    @Mapping(source = "replyAt", target = "createdAt")
    ReplyHostDTO toReplyHostDTO(HostReply hostReply);
}
