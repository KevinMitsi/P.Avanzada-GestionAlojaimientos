package com.avanzada.alojamientos.repositories;

import com.avanzada.alojamientos.entities.FavoriteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<FavoriteEntity, Long> {
    List<FavoriteEntity> findByUserId(Long userId);
    Optional<FavoriteEntity> findByUserIdAndAccommodationId(Long userId, Long accommodationId);
    boolean existsByUserIdAndAccommodationId(Long userId, Long accommodationId);
}
