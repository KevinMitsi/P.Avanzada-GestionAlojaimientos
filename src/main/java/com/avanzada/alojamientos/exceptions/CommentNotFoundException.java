package com.avanzada.alojamientos.exceptions;

import java.util.NoSuchElementException;

public class CommentNotFoundException extends NoSuchElementException {
    public CommentNotFoundException(String message) {
        super(message);
    }
}
