package com.avanzada.alojamientos.controllers.error;

import com.avanzada.alojamientos.DTO.notification.ResponseErrorDTO;
import com.avanzada.alojamientos.exceptions.UnauthorizedException;
import com.avanzada.alojamientos.exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestControllerAdvice
public class ControllerAdvice {
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorizedException(UnauthorizedException ex) {
        return ResponseEntity.status(401).body(ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseErrorDTO> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String paramName = ex.getName(); // nombre del parámetro (ej: accommodationId)

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

}
