package com.avanzada.alojamientos.exceptions;

public class ReservationStateException extends IllegalArgumentException {

    public ReservationStateException(String message) {
        super(message);
    }

    public ReservationStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
