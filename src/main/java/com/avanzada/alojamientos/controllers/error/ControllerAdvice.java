package com.avanzada.alojamientos.controllers.error;

import com.avanzada.alojamientos.DTO.notification.ResponseErrorDTO;
import com.avanzada.alojamientos.exceptions.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ControllerAdvice {

    public static final String KEY_IN_MAP = "detalle";

    // Excepciones de autenticación y autorización
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ResponseErrorDTO> handleUnauthorizedException(UnauthorizedException ex) {
        ResponseErrorDTO error = new ResponseErrorDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Error de autorización",
                Map.of(KEY_IN_MAP, ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(RecoveryTokenException.class)
    public ResponseEntity<ResponseErrorDTO> handleRecoveryTokenException(RecoveryTokenException ex) {
        ResponseErrorDTO error = new ResponseErrorDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Error con el token de recuperación",
                Map.of(KEY_IN_MAP, ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Manejo de credenciales inválidas
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ResponseErrorDTO> handleInvalidPasswordException(InvalidPasswordException ex) {
        ResponseErrorDTO error = new ResponseErrorDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Credenciales inválidas",
                Map.of(KEY_IN_MAP, ex.getMessage(), "tipo", "InvalidPasswordException")
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // Excepciones de reservas
    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<ResponseErrorDTO> handleReservationNotFoundException(ReservationNotFoundException ex) {
        ResponseErrorDTO error = new ResponseErrorDTO(
                HttpStatus.NOT_FOUND.value(),
                "Reserva no encontrada",
                Map.of(KEY_IN_MAP, ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ReservationValidationException.class)
    public ResponseEntity<ResponseErrorDTO> handleReservationValidationException(ReservationValidationException ex) {
        ResponseErrorDTO error = new ResponseErrorDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación en la reserva",
                Map.of(KEY_IN_MAP, ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ReservationPermissionException.class)
    public ResponseEntity<ResponseErrorDTO> handleReservationPermissionException(ReservationPermissionException ex) {
        ResponseErrorDTO error = new ResponseErrorDTO(
                HttpStatus.FORBIDDEN.value(),
                "Permisos insuficientes para esta operación de reserva",
                Map.of(KEY_IN_MAP, ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(ReservationStateException.class)
    public ResponseEntity<ResponseErrorDTO> handleReservationStateException(ReservationStateException ex) {
        ResponseErrorDTO error = new ResponseErrorDTO(
                HttpStatus.CONFLICT.value(),
                "Estado de reserva inválido para esta operación",
                Map.of(KEY_IN_MAP, ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ReservationAvailabilityException.class)
    public ResponseEntity<ResponseErrorDTO> handleReservationAvailabilityException(ReservationAvailabilityException ex) {
        ResponseErrorDTO error = new ResponseErrorDTO(
                HttpStatus.CONFLICT.value(),
                "Alojamiento no disponible",
                Map.of(KEY_IN_MAP, ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // Excepciones de entidades no encontradas
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ResponseErrorDTO> handleUserNotFoundException(UserNotFoundException ex) {
        ResponseErrorDTO error = new ResponseErrorDTO(
                HttpStatus.NOT_FOUND.value(),
                "Usuario no encontrado",
                Map.of(KEY_IN_MAP, ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AccommodationNotFoundException.class)
    public ResponseEntity<ResponseErrorDTO> handleAccommodationNotFoundException(AccommodationNotFoundException ex) {
        ResponseErrorDTO error = new ResponseErrorDTO(
                HttpStatus.NOT_FOUND.value(),
                "Alojamiento no encontrado",
                Map.of(KEY_IN_MAP, ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ResponseErrorDTO> handleCommentNotFoundException(CommentNotFoundException ex) {
        ResponseErrorDTO error = new ResponseErrorDTO(
                HttpStatus.NOT_FOUND.value(),
                "Comentario no encontrado",
                Map.of(KEY_IN_MAP, ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(FavoriteNotFoundException.class)
    public ResponseEntity<ResponseErrorDTO> handleFavoriteNotFoundException(FavoriteNotFoundException ex) {
        ResponseErrorDTO error = new ResponseErrorDTO(
                HttpStatus.NOT_FOUND.value(),
                "Favorito no encontrado",
                Map.of(KEY_IN_MAP, ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ResponseErrorDTO> handleNoSuchElementException(NoSuchElementException ex) {
        ResponseErrorDTO error = new ResponseErrorDTO(
                HttpStatus.NOT_FOUND.value(),
                "Elemento no encontrado",
                Map.of(KEY_IN_MAP, ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // Excepciones de operaciones prohibidas
    @ExceptionHandler(CommentForbiddenException.class)
    public ResponseEntity<ResponseErrorDTO> handleCommentForbiddenException(CommentForbiddenException ex) {
        ResponseErrorDTO error = new ResponseErrorDTO(
                HttpStatus.FORBIDDEN.value(),
                "Operación prohibida en comentarios",
                Map.of(KEY_IN_MAP, ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(FavoriteAlreadyExistsException.class)
    public ResponseEntity<ResponseErrorDTO> handleFavoriteAlreadyExistsException(FavoriteAlreadyExistsException ex) {
        ResponseErrorDTO error = new ResponseErrorDTO(
                HttpStatus.CONFLICT.value(),
                "El favorito ya existe",
                Map.of(KEY_IN_MAP, ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // Excepciones de imágenes
    @ExceptionHandler(UploadingImageException.class)
    public ResponseEntity<ResponseErrorDTO> handleUploadingImageException(UploadingImageException ex) {
        ResponseErrorDTO error = new ResponseErrorDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error al subir la imagen",
                Map.of(KEY_IN_MAP, ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(DeletingImageException.class)
    public ResponseEntity<ResponseErrorDTO> handleDeletingImageException(DeletingImageException ex) {
        ResponseErrorDTO error = new ResponseErrorDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error al eliminar la imagen",
                Map.of(KEY_IN_MAP, ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(ConnectionCloudinaryException.class)
    public ResponseEntity<ResponseErrorDTO> handleConnectionCloudinaryException(ConnectionCloudinaryException ex) {
        ResponseErrorDTO error = new ResponseErrorDTO(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Error de conexión con Cloudinary",
                Map.of(KEY_IN_MAP, ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    // Excepciones de búsqueda
    @ExceptionHandler(SearchingAccommodationException.class)
    public ResponseEntity<ResponseErrorDTO> handleSearchingAccommodationException(SearchingAccommodationException ex) {
        ResponseErrorDTO error = new ResponseErrorDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error al buscar alojamientos",
                Map.of(KEY_IN_MAP, ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // Excepciones de estado ilegal
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ResponseErrorDTO> handleIllegalStateException(IllegalStateException ex) {
        ResponseErrorDTO error = new ResponseErrorDTO(
                HttpStatus.CONFLICT.value(),
                "Estado de operación inválido",
                Map.of(KEY_IN_MAP, ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseErrorDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
        ResponseErrorDTO error = new ResponseErrorDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Argumento inválido",
                Map.of(KEY_IN_MAP, ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Manejador para errores de validación de Jakarta - Bean Validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseErrorDTO> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, Object> validationDetails = new HashMap<>();

        // Obtener información del DTO que falló en la validación
        String objectName = ex.getBindingResult().getObjectName();
        validationDetails.put("dtoName", objectName);

        // Obtener todos los errores de campo
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Valor inválido",
                        (existing, replacement) -> existing + "; " + replacement
                ));

        validationDetails.put("camposConError", fieldErrors);
        validationDetails.put("totalErrores", fieldErrors.size());

        ResponseErrorDTO error = new ResponseErrorDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación en " + objectName,
                validationDetails
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ResponseErrorDTO> handleNotificationNotFoundException(NotificationNotFoundException ex) {
        ResponseErrorDTO error = new ResponseErrorDTO(
                HttpStatus.NOT_FOUND.value(),
                "Notificación no encontrada",
                Map.of(KEY_IN_MAP, ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    // Manejador para validaciones de constrains a nivel de params
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseErrorDTO> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, Object> validationDetails = new HashMap<>();

        Map<String, String> violations = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing + "; " + replacement
                ));

        validationDetails.put("violaciones", violations);
        validationDetails.put("totalViolaciones", violations.size());

        ResponseErrorDTO error = new ResponseErrorDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación de parámetros",
                validationDetails
        );

        return ResponseEntity.badRequest().body(error);
    }

    // Manejador existente para errores de tipo de parámetro
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseErrorDTO> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String paramName = ex.getName();

        String requiredType = Optional.ofNullable(ex.getRequiredType())
                .map(Class::getSimpleName)
                .orElse("desconocido");

        String invalidValue = Objects.toString(ex.getValue(), "null");

        ResponseErrorDTO error = new ResponseErrorDTO(
                HttpStatus.BAD_REQUEST.value(),
                "El parámetro '" + paramName + "' tiene un formato inválido",
                Map.of(
                        "param", paramName,
                        "valorRecibido", invalidValue,
                        "tipoEsperado", requiredType
                )
        );

        return ResponseEntity.badRequest().body(error);
    }

    // Manejador genérico para excepciones no controladas
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseErrorDTO> handleGenericException(Exception ex) {
        ResponseErrorDTO error = new ResponseErrorDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor no mappeado a una excepción específica",
                Map.of(
                        "tipo", ex.getClass().getSimpleName(),
                        KEY_IN_MAP, ex.getMessage() != null ? ex.getMessage() : "Error desconocido"
                )
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

}
