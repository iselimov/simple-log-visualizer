package com.defrag.log.visualizer.common.rest;

public class ValidationException extends RuntimeException {

    public ValidationException() {
    }

    public ValidationException(final String message) {
        super(message);
    }
}
