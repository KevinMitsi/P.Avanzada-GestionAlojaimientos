package com.avanzada.alojamientos.repositories;

import com.avanzada.alojamientos.entities.CityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<CityEntity, Long> {
}
