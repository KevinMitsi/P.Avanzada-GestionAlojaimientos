package com.avanzada.alojamientos.mappers;


import com.avanzada.alojamientos.DTO.accommodation.CreateAccommodationDTO;
import com.avanzada.alojamientos.DTO.accommodation.AccommodationDTO;
import com.avanzada.alojamientos.DTO.accommodation.CreateAccommodationResponseDTO;
import com.avanzada.alojamientos.DTO.accommodation.UpdateAccommodationDTO;
import com.avanzada.alojamientos.entities.AccommodationEntity;
import com.avanzada.alojamientos.entities.CityEntity;
import org.mapstruct.*;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccommodationMapper {


    @Mapping(source = "host.id", target = "hostId")
    @Mapping(source = "city", target = "city")
    @Mapping(source = "images", target = "images")
    @Mapping(source = "coordinates", target = "coordinates")
    AccommodationDTO toAccommodationDTO(AccommodationEntity accommodation);

    @Mapping(source = "host.id", target = "hostId")
    @Mapping(source = "city", target = "city")
    @Mapping(source = "coordinates", target = "coordinates")
    CreateAccommodationResponseDTO toCreateAccommodationResponseDTO(AccommodationEntity accommodation);



    // Create DTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "softDeleted", constant = "false")
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "host", ignore = true)
    @Mapping(source = "city", target = "city")
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "favorites", ignore = true)
    @Mapping(target = "countReservations", ignore = true)
    @Mapping(target = "avgRating", ignore = true)
    AccommodationEntity  toEntity(CreateAccommodationDTO createAccommodationDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "host", ignore = true)
    @Mapping(target = "city", ignore = true) // Ignore city updates for now
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "favorites", ignore = true)
    @Mapping(target = "countReservations", ignore = true)
    @Mapping(target = "softDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(UpdateAccommodationDTO dto, @MappingTarget AccommodationEntity entity);

    default CityEntity map(Long cityId) {
        if (cityId == null) {
            return null;
        }
        CityEntity city = new CityEntity();
        city.setId(cityId);
        return city;
    }

}
