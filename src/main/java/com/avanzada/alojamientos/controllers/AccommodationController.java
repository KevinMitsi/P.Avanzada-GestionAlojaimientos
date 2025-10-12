package com.avanzada.alojamientos.controllers;


import com.avanzada.alojamientos.DTO.accommodation.*;
import com.avanzada.alojamientos.DTO.notification.ResponseErrorDTO;
import com.avanzada.alojamientos.services.AccommodationService;
import com.avanzada.alojamientos.services.impl.AccommodationServiceImpl;
import com.avanzada.alojamientos.exceptions.UploadingStorageException;
import com.avanzada.alojamientos.exceptions.DeletingStorageException;
import com.avanzada.alojamientos.security.CurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/accommodations")
@RequiredArgsConstructor
@Tag(name = "Alojamientos", description = "Endpoints para la gestión de alojamientos")
public class AccommodationController {

    private final AccommodationService accommodationService;
    private final CurrentUserService currentUserService;

    @PostMapping
    @Operation(
            summary = "Crear un nuevo alojamiento",
            description = "Permite a un host autenticado crear un nuevo alojamiento. Requiere rol HOST.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Alojamiento creado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateAccommodationResponseDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "id": 1,
                                        "title": "Apartamento en el centro",
                                        "message": "Alojamiento creado exitosamente"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "code": 400,
                                        "message": "Error de validación en CreateAccommodationDTO",
                                        "details": {
                                            "camposConError": {
                                                "title": "El título debe tener entre 5 y 200 caracteres",
                                                "pricePerNight": "El precio debe ser positivo"
                                            }
                                        }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token JWT inválido o ausente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado - Se requiere rol HOST",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDTO.class)
                    )
            )
    })
    public ResponseEntity<CreateAccommodationResponseDTO> create(@RequestBody @Valid CreateAccommodationDTO dto) {
        Long hostId = currentUserService.getCurrentHostId();
        CreateAccommodationResponseDTO result = accommodationService.create(dto, hostId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{accommodationId}")
    @Operation(
            summary = "Actualizar un alojamiento",
            description = "Permite al host propietario actualizar la información de su alojamiento. Requiere rol HOST.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Alojamiento actualizado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccommodationDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - No eres el propietario del alojamiento",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Alojamiento no encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDTO.class)
                    )
            )
    })
    public ResponseEntity<AccommodationDTO> update(
            @Parameter(description = "ID del alojamiento a actualizar", required = true, example = "1")
            @PathVariable Long accommodationId,
            @RequestBody @Valid UpdateAccommodationDTO dto) {
        Long userId = currentUserService.getCurrentUserId();
        AccommodationDTO result = accommodationService.update(userId, accommodationId, dto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    @Operation(
            summary = "Buscar alojamientos",
            description = "Busca alojamientos según criterios de búsqueda. Este endpoint es público y no requiere autenticación. Soporta paginación y filtros por ciudad, fechas, número de huéspedes, rango de precios y servicios."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Búsqueda realizada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parámetros de búsqueda inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno al realizar la búsqueda",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "code": 500,
                                        "message": "Error al buscar alojamientos",
                                        "details": {
                                            "detalle": "Error en la consulta a la base de datos"
                                        }
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<Page<AccommodationDTO>> search(
            @Parameter(description = "ID de la ciudad", example = "1")
            @RequestParam(required = false) Long cityId,
            @Parameter(description = "Fecha de inicio (formato: YYYY-MM-DD)", example = "2025-01-15")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "Fecha de fin (formato: YYYY-MM-DD)", example = "2025-01-20")
            @RequestParam(required = false) String endDate,
            @Parameter(description = "Número de huéspedes", example = "2")
            @RequestParam(required = false) Integer guests,
            @Parameter(description = "Precio mínimo por noche", example = "50.00")
            @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Precio máximo por noche", example = "200.00")
            @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Lista de servicios requeridos", example = "[\"WiFi\", \"Piscina\"]")
            @RequestParam(required = false) List<String> services,
            @Parameter(description = "Parámetros de paginación (page, size, sort)", example = "page=0&size=10&sort=pricePerNight,asc")
            Pageable pageable) {

        Page<AccommodationDTO> result = accommodationService.search(generateCriteria(cityId,
                startDate, endDate, guests, minPrice, maxPrice, services), pageable);
        return ResponseEntity.ok(result);
    }



    @DeleteMapping("/{accommodationId}")
    @Operation(
            summary = "Eliminar un alojamiento (soft delete)",
            description = "Realiza un borrado lógico del alojamiento. Solo el host propietario puede eliminar su alojamiento. Requiere rol HOST.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Alojamiento eliminado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - No eres el propietario del alojamiento",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Alojamiento no encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDTO.class)
                    )
            )
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID del alojamiento a eliminar", required = true, example = "1")
            @PathVariable Long accommodationId) {
        Long userId = currentUserService.getCurrentUserId();
        accommodationService.delete(userId, accommodationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/host/me")
    @Operation(
            summary = "Obtener alojamientos del host autenticado",
            description = "Devuelve una lista paginada de todos los alojamientos que pertenecen al host autenticado. Requiere rol HOST.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de alojamientos obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Se requiere rol HOST",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDTO.class)
                    )
            )
    })
    public ResponseEntity<Page<AccommodationDTO>> findByHost(
            @Parameter(description = "Parámetros de paginación", example = "page=0&size=10")
            Pageable pageable) {
        Long hostId = currentUserService.getCurrentUserId();
        Page<AccommodationDTO> result = accommodationService.findByHost(hostId, pageable);
        return ResponseEntity.ok(result);
    }


    @PostMapping(value = "/{accommodationId}/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Subir imágenes a un alojamiento",
            description = "Permite al host propietario subir una o más imágenes a su alojamiento. Las imágenes se almacenan en Cloudinary. Requiere rol HOST.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Imágenes subidas exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = List.class),
                            examples = @ExampleObject(value = """
                                    [
                                        "https://res.cloudinary.com/.../image1.jpg",
                                        "https://res.cloudinary.com/.../image2.jpg"
                                    ]
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Archivos inválidos o no se proporcionaron imágenes",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - No eres el propietario del alojamiento",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Alojamiento no encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error al subir las imágenes a Cloudinary",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "code": 500,
                                        "message": "Error al subir la imagen",
                                        "details": {
                                            "detalle": "Error de conexión con Cloudinary"
                                        }
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<List<String>> uploadImages(
            @Parameter(description = "ID del alojamiento", required = true, example = "1")
            @PathVariable Long accommodationId,
            @Parameter(description = "Lista de archivos de imagen a subir", required = true)
            @RequestParam("images") List<MultipartFile> imageFiles,
            @Parameter(description = "Indica si es imagen principal", example = "false")
            @RequestParam(defaultValue = "false") boolean primary) throws UploadingStorageException {
        Long userId = currentUserService.getCurrentUserId();
        List<String> uploadedUrls = ((AccommodationServiceImpl) accommodationService)
                    .uploadAndAddImages(userId, accommodationId, imageFiles, primary);
        return ResponseEntity.ok(uploadedUrls);
    }

    @DeleteMapping("/{accommodationId}/images/{imageId}")
    @Operation(
            summary = "Eliminar una imagen del alojamiento",
            description = "Permite al host propietario eliminar una imagen específica de su alojamiento. La imagen se elimina tanto de la base de datos como de Cloudinary. Requiere rol HOST.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Imagen eliminada exitosamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - No eres el propietario del alojamiento",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Alojamiento o imagen no encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error al eliminar la imagen de Cloudinary",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "code": 500,
                                        "message": "Error al eliminar la imagen",
                                        "details": {
                                            "detalle": "No se pudo eliminar la imagen del almacenamiento"
                                        }
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<Void> deleteImageFromCloudinary(
            @Parameter(description = "ID del alojamiento", required = true, example = "1")
            @PathVariable Long accommodationId,
            @Parameter(description = "ID de la imagen a eliminar", required = true, example = "5")
            @PathVariable Long imageId) throws DeletingStorageException {
        Long userId = currentUserService.getCurrentUserId();
        ((AccommodationServiceImpl) accommodationService).deleteImageFromCloudinary(userId, accommodationId, imageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{accommodationId}/metrics")
    @Operation(
            summary = "Obtener métricas de un alojamiento",
            description = "Devuelve métricas y estadísticas del alojamiento como número de reservas, ocupación, ingresos, etc. Endpoint público.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Métricas obtenidas exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccommodationMetrics.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Alojamiento no encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDTO.class)
                    )
            )
    })
    public ResponseEntity<AccommodationMetrics> getMetrics(
            @Parameter(description = "ID del alojamiento", required = true, example = "1")
            @PathVariable Long accommodationId,
            @Parameter(description = "Fecha de inicio del período (formato: YYYY-MM-DD)", example = "2025-01-01")
            @RequestParam(name = "start", required = false) LocalDate startDate,
            @Parameter(description = "Fecha de fin del período (formato: YYYY-MM-DD)", example = "2025-12-31")
            @RequestParam(name = "end", required = false) LocalDate endDate){
        AccommodationMetrics result = accommodationService.getMetrics(accommodationId, startDate, endDate);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{accommodationId}")
    @Operation(
            summary = "Obtener un alojamiento por ID",
            description = "Devuelve la información completa de un alojamiento específico. Endpoint público."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Alojamiento encontrado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccommodationDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Alojamiento no encontrado"
            )
    })
    public ResponseEntity<AccommodationDTO> findById(
            @Parameter(description = "ID del alojamiento", required = true, example = "1")
            @PathVariable Long accommodationId) {
        Optional<AccommodationDTO> result = accommodationService.findById(accommodationId);
        return result.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    private static AccommodationSearch generateCriteria(Long cityId, String startDate, String endDate, Integer guests, BigDecimal minPrice, BigDecimal maxPrice, List<String> services) {
        return new AccommodationSearch(
                cityId,
                startDate != null ? LocalDate.parse(startDate) : null,
                endDate != null ? LocalDate.parse(endDate) : null,
                guests,
                minPrice,
                maxPrice,
                services
        );
    }
}
