package com.trongtin.spabooking.exception;

public class AuthenticationException extends BookingException {

    public AuthenticationException(String message) {
        super("AUTHENTICATION_ERROR", message);
    }
}