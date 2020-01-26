package com.defrag.log.visualizer.rest.exception;

public class ValidationException extends RuntimeException {

    public ValidationException() {
    }

    public ValidationException(final String message) {
        super(message);
    }
}
