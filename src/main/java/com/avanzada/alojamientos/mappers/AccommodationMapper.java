package com.avanzada.alojamientos.mappers;


import com.avanzada.alojamientos.DTO.CreateAccommodationDTO;
import com.avanzada.alojamientos.DTO.AccommodationDTO;
import com.avanzada.alojamientos.entities.AccommodationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;




@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccommodationMapper {


    @Mapping(source = "host.id", target = "hostId")
    @Mapping(source = "city", target = "city")
    @Mapping(source = "images", target = "images")
    @Mapping(source = "coordinates", target = "coordinates")
    AccommodationDTO toAccommodationDTO(AccommodationEntity accommodation);



    // Create DTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "softDeleted", constant = "false")
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "host", ignore = true) // Se asignará en el servicio
    @Mapping(target = "city", ignore = true) // Se asignará en el servicio usando cityId
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "favorites", ignore = true)
    @Mapping(target = "countReservations", ignore = true)
    @Mapping(target = "avgRating", ignore = true)
    AccommodationEntity toEntity(CreateAccommodationDTO createAccommodationDTO);





}
