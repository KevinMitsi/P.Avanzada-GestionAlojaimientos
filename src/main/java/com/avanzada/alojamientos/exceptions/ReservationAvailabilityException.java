package com.avanzada.alojamientos.exceptions;

public class ReservationAvailabilityException extends IllegalArgumentException {

    public ReservationAvailabilityException(String message) {
        super(message);
    }

    public ReservationAvailabilityException(String message, Throwable cause) {
        super(message, cause);
    }
}
