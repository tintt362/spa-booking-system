package com.trongtin.spabooking.exception;


public class SlotNotAvailableException extends BookingException {

    public SlotNotAvailableException() {
        super("SLOT_NOT_AVAILABLE", "Khung giờ này đã được đặt. Vui lòng chọn giờ khác.");
    }

    public SlotNotAvailableException(String message) {
        super("SLOT_NOT_AVAILABLE", message);
    }
}