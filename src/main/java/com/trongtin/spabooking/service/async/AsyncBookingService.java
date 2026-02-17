package com.trongtin.spabooking.service.async;


import com.trongtin.spabooking.entity.Booking;
import com.trongtin.spabooking.service.email.EmailService;
import com.trongtin.spabooking.service.email.EmailTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncBookingService {

    private final EmailService emailService;
    private final EmailTemplate emailTemplate;


    // Send booking confirmation email (async)
    @Async("taskExecutor")
    public void sendBookingConfirmationEmail(Booking booking) {
        log.info("Async: Sending confirmation email for booking: {}",
                booking.getBookingId());

        try {
            if (booking.getCustomerEmail() != null &&
                    !booking.getCustomerEmail().isEmpty()) {

                String htmlContent = emailTemplate.bookingConfirmation(booking);

                emailService.sendHtmlEmail(
                        booking.getCustomerEmail(),
                        "Xác nhận đặt lịch - " + booking.getBookingId(),
                        htmlContent
                );

                log.info("Confirmation email sent for booking: {}",
                        booking.getBookingId());
            }
        } catch (Exception e) {
            log.error("Failed to send confirmation email for booking {}: {}",
                    booking.getBookingId(), e.getMessage());
            // Don't throw - async operation should not affect main flow
        }
    }


    //Send cancellation email (async)
    @Async("taskExecutor")
    public void sendCancellationEmail(Booking booking) {
        log.info("Async: Sending cancellation email for booking: {}",
                booking.getBookingId());

        try {
            if (booking.getCustomerEmail() != null &&
                    !booking.getCustomerEmail().isEmpty()) {

                String htmlContent = emailTemplate.bookingCancellation(booking);

                emailService.sendHtmlEmail(
                        booking.getCustomerEmail(),
                        "Xác nhận hủy lịch - " + booking.getBookingId(),
                        htmlContent
                );

                log.info("Cancellation email sent for booking: {}",
                        booking.getBookingId());
            }
        } catch (Exception e) {
            log.error("Failed to send cancellation email for booking {}: {}",
                    booking.getBookingId(), e.getMessage());
        }
    }


    //Send reminder email (async)
    @Async("taskExecutor")
    public void sendReminderEmail(Booking booking) {
        log.info("Async: Sending reminder email for booking: {}",
                booking.getBookingId());

        try {
            if (booking.getCustomerEmail() != null &&
                    !booking.getCustomerEmail().isEmpty()) {

                String htmlContent = emailTemplate.bookingReminder(booking);

                emailService.sendHtmlEmail(
                        booking.getCustomerEmail(),
                        "Nhắc nhở: Lịch hẹn ngày mai",
                        htmlContent
                );

                log.info("Reminder email sent for booking: {}",
                        booking.getBookingId());
            }
        } catch (Exception e) {
            log.error("Failed to send reminder email for booking {}: {}",
                    booking.getBookingId(), e.getMessage());
        }
    }

    //Notify admin about new booking (async)

    @Async("taskExecutor")
    public void notifyAdminNewBooking(Booking booking) {
        log.info("Async: Notifying admin about new booking: {}",
                booking.getBookingId());

        try {
            String content = String.format("""
                            Booking mới:
                            
                            Mã: %s
                            Khách hàng: %s
                            SĐT: %s
                            Dịch vụ: %s
                            Ngày: %s
                            Giờ: %s
                            
                            Vui lòng xác nhận booking này.
                            """,
                    booking.getBookingId(),
                    booking.getCustomerName(),
                    booking.getCustomerPhone(),
                    booking.getService().getName(),
                    booking.getBookingDate(),
                    booking.getBookingTime()
            );

            emailService.sendTextEmail(
                    "admin@spa.com",
                    "Booking mới - " + booking.getBookingId(),
                    content
            );

            log.info("Admin notified about booking: {}", booking.getBookingId());

        } catch (Exception e) {
            log.error("Failed to notify admin about booking {}: {}",
                    booking.getBookingId(), e.getMessage());
        }
    }
}