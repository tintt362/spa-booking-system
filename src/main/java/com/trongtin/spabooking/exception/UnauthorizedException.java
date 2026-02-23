package com.trongtin.spabooking.exception;


public class UnauthorizedException extends BookingException {

    public UnauthorizedException() {
        super("UNAUTHORIZED", "Bạn không có quyền thực hiện thao tác này");
    }

    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }
}