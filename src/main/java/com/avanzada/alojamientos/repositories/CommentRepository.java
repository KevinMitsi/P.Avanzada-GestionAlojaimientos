package com.avanzada.alojamientos.repositories;

import com.avanzada.alojamientos.entities.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    boolean existsByReservation_Id(Long reservationId);
    Page<CommentEntity> findAllByAccommodation_Id(Long accommodationId, Pageable pageable);

    Page<CommentEntity> findAllByUser_Id(Long userId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM CommentEntity c WHERE c.id = ?1")
    void deleteComment(Long commentId);

    /**
     * Calcula el rating promedio de un alojamiento basado en sus comentarios
     */
    @Query("SELECT COALESCE(AVG(CAST(c.rating AS double)), 0.0) FROM CommentEntity c WHERE c.accommodation.id = :accommodationId")
    Double findAverageRatingByAccommodationId(@Param("accommodationId") Long accommodationId);
}