package com.avanzada.alojamientos.repositories;

import com.avanzada.alojamientos.entities.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
}