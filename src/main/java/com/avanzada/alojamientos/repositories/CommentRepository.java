package com.avanzada.alojamientos.repositories;

import com.avanzada.alojamientos.entities.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    boolean existsByReservation_Id(Long reservationId);
    Page<CommentEntity> findAllByAccommodation_Id(Long accommodationId, Pageable pageable);

    Page<CommentEntity> findAllByUser_Id(Long userId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM CommentEntity c WHERE c.id = ?1")
    void deleteComment(Long commentId);
}