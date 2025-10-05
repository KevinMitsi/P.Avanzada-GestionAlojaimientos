package com.avanzada.alojamientos.exceptions;

public class ReservationNotFoundException extends IllegalArgumentException {

    public ReservationNotFoundException(String message) {
        super(message);
    }

    public ReservationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReservationNotFoundException(Long reservationId) {
        super("Reservation with ID " + reservationId + " not found");
    }
}
