package com.trongtin.spabooking.exception;


public class ResourceNotFoundException extends BookingException {

    public ResourceNotFoundException(String resource) {
        super("RESOURCE_NOT_FOUND", resource + " không tồn tại");
    }

    public ResourceNotFoundException(String errorCode, String message) {
        super(errorCode, message);
    }
}