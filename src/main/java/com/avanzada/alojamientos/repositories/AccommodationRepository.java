package com.avanzada.alojamientos.repositories;

import com.avanzada.alojamientos.entities.AccommodationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AccommodationRepository extends JpaRepository<AccommodationEntity, Long> {

    /**
     * Busca un alojamiento por ID cargando eagerly las imágenes para evitar LazyInitializationException
     */
    @EntityGraph(attributePaths = {"images", "host", "city", "services", "reservations", "comments"})
    @NonNull
    Optional<AccommodationEntity> findById(@NonNull Long id);

    /**
     * Busca alojamientos aplicando filtros opcionales:
     * - city (busca por nombre parcialmente, case-insensitive)
     * - rango de precio (minPrice, maxPrice)
     * - capacidad de huéspedes mínima (guests)
     * - disponibilidad entre startDate y endDate (no retornar alojamientos con reservas que se solapen)
     * <p>
     * No incluye filtrado por servicios; usar searchWithServices si se necesitan servicios exactos.
     */
    @Query("""
        SELECT a
        FROM AccommodationEntity a
        WHERE a.softDeleted = false
          AND (:city IS NULL OR LOWER(a.city.name) LIKE LOWER(CONCAT('%', :city, '%')))
          AND (:minPrice IS NULL OR a.pricePerNight >= :minPrice)
          AND (:maxPrice IS NULL OR a.pricePerNight <= :maxPrice)
          AND (:guests IS NULL OR a.maxGuests >= :guests)
          AND (
                :startDate IS NULL OR :endDate IS NULL
                OR NOT EXISTS (
                    SELECT r FROM com.avanzada.alojamientos.entities.ReservationEntity r
                    WHERE r.accommodation = a
                      AND (r.status IS NULL OR r.status NOT IN ('CANCELLED', 'CANCEL'))
                      AND r.startDate <= :endDate
                      AND r.endDate >= :startDate
                )
              )
        """)
    Page<AccommodationEntity> search(
            @Param("city") Long city,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("guests") Integer guests,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    /**
     * Búsqueda que filtra también por servicios.
     * Se espera que `a.services` sea una colección (por ejemplo Set<String> o entidad embebida)
     * y que se pase la lista de servicios requeridos en 'services'. La consulta agrupa por alojamiento
     * y exige que el número de servicios distintos encontrados sea igual al tamaño del conjunto requerido,
     * esto asegura que el alojamiento contiene *todos* los servicios solicitados.
     */
    @Query("""
        SELECT a
        FROM AccommodationEntity a
        JOIN a.services s
        WHERE a.softDeleted = false
          AND (:city IS NULL OR LOWER(a.city.name) LIKE LOWER(CONCAT('%', :city, '%')))
          AND (:minPrice IS NULL OR a.pricePerNight >= :minPrice)
          AND (:maxPrice IS NULL OR a.pricePerNight <= :maxPrice)
          AND (:guests IS NULL OR a.maxGuests >= :guests)
          AND (
                :startDate IS NULL OR :endDate IS NULL
                OR NOT EXISTS (
                    SELECT r FROM com.avanzada.alojamientos.entities.ReservationEntity r
                    WHERE r.accommodation = a
                      AND (r.status IS NULL OR r.status NOT IN ('CANCELLED', 'CANCEL'))
                      AND r.startDate <= :endDate
                      AND r.endDate >= :startDate
                )
              )
          AND s IN :services
        GROUP BY a
        HAVING COUNT(DISTINCT s) = :servicesSize
        """)
    Page<AccommodationEntity> searchWithServices(
            @Param("city") Long city,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("guests") Integer guests,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("services") List<String> services,
            @Param("servicesSize") long servicesSize,
            Pageable pageable
    );

    /**
     * Buscar alojamientos de un host (no eliminados)
     */
    Page<AccommodationEntity> findByHostIdAndSoftDeletedFalse(Long hostId, Pageable pageable);

    /**
     * Contar reservas futuras no-canceladas para un alojamiento (útil para validar eliminación).
     */
    @Query("""
        SELECT COUNT(r)
        FROM com.avanzada.alojamientos.entities.ReservationEntity r
        WHERE r.accommodation.id = :accommodationId
          AND r.startDate > :fromDate
          AND (r.status IS NULL OR r.status NOT IN ('CANCELLED', 'CANCEL'))
        """)
    long countFutureNonCancelledReservations(
            @Param("accommodationId") Long accommodationId,
            @Param("fromDate") LocalDate fromDate
    );
}
