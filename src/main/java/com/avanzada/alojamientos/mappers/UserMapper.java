package com.avanzada.alojamientos.mappers;

import com.avanzada.alojamientos.DTO.user.CreateUserDTO;
import com.avanzada.alojamientos.DTO.user.UserDTO;

import com.avanzada.alojamientos.entities.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    // Entity -> DTO
    UserDTO toUserDTO(UserEntity user);

    // Create DTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "verified", constant = "false")
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "accommodations", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "favorites", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "hostProfile", ignore = true)
    UserEntity toEntity(CreateUserDTO createUserDTO);

}
