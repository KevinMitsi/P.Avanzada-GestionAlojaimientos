package com.avanzada.alojamientos.mappers;

import com.avanzada.alojamientos.DTO.user.CreateUserDTO;
import com.avanzada.alojamientos.DTO.user.EditUserDTO;
import com.avanzada.alojamientos.DTO.auth.RegisterUserDTO;
import com.avanzada.alojamientos.DTO.user.UserDTO;

import com.avanzada.alojamientos.entities.UserEntity;
import org.mapstruct.*;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    // Entity -> DTO
    @Mapping(target = "profileImageUrl", expression = "java(user.getProfileImage() != null ? user.getProfileImage().getUrl() : null)")
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
    @Mapping(target = "profileImage", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "documentsUrl", ignore = true)
    @Mapping(target = "dateOfBirth", source = "dateBirth")
    @Mapping(target = "roles", ignore = true)
    UserEntity toEntity(CreateUserDTO createUserDTO);

    // Register DTO -> Entity
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
    @Mapping(target = "profileImage", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "documentsUrl", ignore = true)
    @Mapping(target = "roles", ignore = true)
    UserEntity toEntity(RegisterUserDTO registerUserDTO);

    // Update Entity from EditUserDTO
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "accommodations", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "favorites", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "hostProfile", ignore = true)
    @Mapping(target = "profileImage", ignore = true)
    @Mapping(target = "documentsUrl", ignore = true)
    @Mapping(target = "dateOfBirth", source = "dateBirth")
    @Mapping(target = "roles", ignore = true)
    void updateEntityFromDTO(EditUserDTO editUserDTO, @MappingTarget UserEntity user);

}
