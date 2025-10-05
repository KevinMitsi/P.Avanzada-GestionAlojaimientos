package com.avanzada.alojamientos.exceptions;

public class ReservationPermissionException extends IllegalArgumentException {

    public ReservationPermissionException(String message) {
        super(message);
    }

    public ReservationPermissionException(String message, Throwable cause) {
        super(message, cause);
    }
}
