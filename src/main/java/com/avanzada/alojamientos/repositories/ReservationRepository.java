package com.avanzada.alojamientos.repositories;

import com.avanzada.alojamientos.DTO.model.ReservationStatus;
import com.avanzada.alojamientos.entities.ReservationEntity;
import com.avanzada.alojamientos.mappers.ReservationMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    // buscar reserva por id
    Optional<ReservationEntity> findById(Long id);

    //busca las reserva que tiene un alojamiento
    @Query("SELECT r FROM ReservationEntity r WHERE " +
            "r.accommodation.id = :accommodationId AND " +
            "r.status != 'CANCELLED' AND " +
            "((r.startDate < :endDate) AND (r.endDate > :startDate))")
    List<ReservationEntity> findOverlappingReservations(
            @Param("accommodationId") Long accommodationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
    SELECT r FROM ReservationEntity r
    JOIN r.accommodation a
    JOIN a.host h
    WHERE (:userId IS NULL OR r.user.id = :userId)
      AND (:hostId IS NULL OR h.id = :hostId)
      AND (:accommodationId IS NULL OR a.id = :accommodationId)
      AND (:status IS NULL OR r.status = :status)
      AND (:startDate IS NULL OR r.startDate >= :startDate)
      AND (:endDate IS NULL OR r.endDate <= :endDate)
""")
    Page<ReservationEntity> searchReservations(
            @Param("userId") Long userId,
            @Param("hostId") Long hostId,
            @Param("accommodationId") Long accommodationId,
            @Param("status") ReservationStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("""
    SELECT r FROM ReservationEntity r
    JOIN r.accommodation a
    WHERE (:accommodationId IS NULL OR a.id = :accommodationId)
""")
    Page<ReservationEntity> findByAccommodation(
            @Param("accommodationId") Long accommodationId,
            Pageable pageable
    );

    @Query("""
    SELECT r FROM ReservationEntity r
    WHERE r.user.id = :userId
""")
    Page<ReservationEntity> findByUser(
            @Param("userId") Long userId,
            Pageable pageable
    );



}
