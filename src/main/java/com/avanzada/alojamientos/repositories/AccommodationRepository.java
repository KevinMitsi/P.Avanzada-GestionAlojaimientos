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


    Boolean existsByIdAndSoftDeletedFalse(Long id);

    /**
     * Busca un alojamiento no eliminado (soft deleted = false) por ID
     */
    @EntityGraph(attributePaths = {"images", "host", "city", "services"})
    Optional<AccommodationEntity> findByIdAndSoftDeletedFalse(Long id);

    /**
     * Busca un alojamiento por ID cargando eagerly las imágenes para evitar LazyInitializationException
     */
    @EntityGraph(attributePaths = {"images", "host", "city", "services"})
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
    @EntityGraph(attributePaths = {"images", "host", "city", "services"})
    @Query("""
        SELECT a
        FROM AccommodationEntity a
        WHERE a.softDeleted = false
          AND (:cityName IS NULL OR :cityName = '' OR LOWER(a.city.name) LIKE LOWER(CONCAT('%', :cityName, '%')))
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
            @Param("cityName") String cityName,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("guests") Integer guests,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    /**
     * Búsqueda que filtra también por servicios.
     * NOTA: Este método ya NO usa consulta JPQL, se implementa con lógica Java en el servicio.
     * Se mantiene la firma para compatibilidad pero la implementación se hace en AccommodationServiceImpl.
     */
    default List<Long> findAccommodationIdsWithServices(
            String cityName,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer guests,
            LocalDate startDate,
            LocalDate endDate,
            List<String> services,
            long servicesSize,
            Pageable pageable
    ) {
        // Este método ahora se implementa con lógica Java en el servicio
        // Mantener como default method para evitar romper la interfaz
        throw new UnsupportedOperationException("Este método debe ser llamado desde AccommodationServiceImpl");
    }

    /**
     * Buscar alojamientos por ID con EntityGraph
     */
    @EntityGraph(attributePaths = {"images", "host", "city", "services"})
    @Query("SELECT a FROM AccommodationEntity a WHERE a.id IN :ids")
    List<AccommodationEntity> findByIdsWithEntityGraph(@Param("ids") List<Long> ids);

    /**
     * Buscar alojamientos de un host (no eliminados) - solo carga images y datos básicos
     */
    @EntityGraph(attributePaths = {"images", "host", "city", "services"})
    Page<AccommodationEntity> findByHostIdAndSoftDeletedFalse(Long hostId, Pageable pageable);


    @EntityGraph(attributePaths = {"images", "host", "city", "services"})
    @NonNull
    Page<AccommodationEntity> findAll(@NonNull Pageable pageable);

    /**
     * Obtener TODOS los alojamientos con todas las relaciones cargadas (modo arcaico para filtrado manual)
     * NOTA: No se puede cargar 'reservations' junto con 'images' porque ambas son List (bags)
     * y Hibernate lanza MultipleBagFetchException. Se cargan las reservations por separado.
     */
    @EntityGraph(attributePaths = {"images", "host", "city", "services"})
    @Query("SELECT a FROM AccommodationEntity a")
    List<AccommodationEntity> findAllWithBasicRelations();

    /**
     * Cargar solo las reservaciones de todos los alojamientos (para filtrado manual)
     */
    @Query("SELECT DISTINCT a FROM AccommodationEntity a LEFT JOIN FETCH a.reservations")
    List<AccommodationEntity> findAllWithReservations();

    /**
     * Busca un alojamiento por ID con reservations para métricas
     */
    @EntityGraph(attributePaths = {"reservations", "host", "city"})
    @Query("SELECT a FROM AccommodationEntity a WHERE a.id = :id")
    Optional<AccommodationEntity> findByIdWithReservations(@Param("id") Long id);

    /**
     * Busca un alojamiento por ID con comments para métricas
     */
    @EntityGraph(attributePaths = {"comments", "host", "city"})
    @Query("SELECT a FROM AccommodationEntity a WHERE a.id = :id")
    Optional<AccommodationEntity> findByIdWithComments(@Param("id") Long id);

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

    /**
     * Obtener todos los servicios únicos de todos los alojamientos activos
     */
    @Query("""
        SELECT DISTINCT s
        FROM AccommodationEntity a
        JOIN a.services s
        WHERE a.softDeleted = false
        """)
    List<String> findAllUniqueServices();

    Boolean existsByIdAndHostId(Long accommodationId, Long hostId);
}
