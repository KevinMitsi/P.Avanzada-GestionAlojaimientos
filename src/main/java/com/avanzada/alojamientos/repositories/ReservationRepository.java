package com.avanzada.alojamientos.repositories;

import com.avanzada.alojamientos.entities.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
}
