package com.trongtin.spabooking.exception;

public class ValidationException extends BookingException {

    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }
}