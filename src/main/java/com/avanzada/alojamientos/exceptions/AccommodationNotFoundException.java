package com.avanzada.alojamientos.exceptions;



import java.util.NoSuchElementException;

public class AccommodationNotFoundException extends NoSuchElementException {
    public AccommodationNotFoundException(String message) {
        super(message);
    }
}
