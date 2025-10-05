package com.avanzada.alojamientos.exceptions;

public class ReservationValidationException extends IllegalArgumentException {

    public ReservationValidationException(String message) {
        super(message);
    }

    public ReservationValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
