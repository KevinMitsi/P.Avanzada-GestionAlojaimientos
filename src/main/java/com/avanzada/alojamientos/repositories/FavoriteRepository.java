package com.avanzada.alojamientos.repositories;

import com.avanzada.alojamientos.entities.FavoriteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<FavoriteEntity, Long> {
}

