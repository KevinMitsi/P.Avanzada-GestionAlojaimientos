package com.avanzada.alojamientos.repositories;

import com.avanzada.alojamientos.entities.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<ImageEntity, Long> {
}
