package com.trongtin.spabooking.service.email;

// EmailTemplate.java


import com.trongtin.spabooking.entity.Booking;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class EmailTemplate {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Booking confirmation email
     */
    public String bookingConfirmation(Booking booking) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background: #f9f9f9; }
                    .booking-info { background: white; padding: 15px; margin: 15px 0; border-left: 4px solid #4CAF50; }
                    .info-row { margin: 10px 0; }
                    .label { font-weight: bold; color: #666; }
                    .value { color: #333; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    .button { 
                        display: inline-block; 
                        padding: 12px 30px; 
                        background: #4CAF50; 
                        color: white; 
                        text-decoration: none; 
                        border-radius: 5px; 
                        margin: 15px 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Xác Nhận Đặt Lịch</h1>
                    </div>
                    
                    <div class="content">
                        <p>Xin chào <strong>%s</strong>,</p>
                        
                        <p>Cảm ơn bạn đã đặt lịch tại spa của chúng tôi! Dưới đây là thông tin chi tiết:</p>
                        
                        <div class="booking-info">
                            <div class="info-row">
                                <span class="label">Mã đặt lịch:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Dịch vụ:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Ngày:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Giờ:</span>
                                <span class="value">%s - %s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Therapist:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Giá:</span>
                                <span class="value">%,d VNĐ</span>
                            </div>
                        </div>
                        
                        <p><strong>Lưu ý quan trọng:</strong></p>
                        <ul>
                            <li>Vui lòng đến trước giờ hẹn 10 phút</li>
                            <li>Nếu cần hủy lịch, vui lòng thông báo trước ít nhất 2 giờ</li>
                            <li>Mang theo mã đặt lịch khi đến spa</li>
                        </ul>
                        
                        <center>
                            <a href="https://spa-booking.com/bookings/%s" class="button">
                                Xem Chi Tiết
                            </a>
                        </center>
                    </div>
                    
                    <div class="footer">
                        <p>Địa chỉ: 123 Đường ABC, Quận 1, TP.HCM</p>
                        <p>Hotline: 1900 xxxx</p>
                        <p>Email: support@spa-booking.com</p>
                    </div>
                </div>
            </body>
            </html>
            """,
                booking.getCustomerName(),
                booking.getBookingId(),
                booking.getService().getName(),
                booking.getBookingDate().format(DATE_FORMATTER),
                booking.getBookingTime().format(TIME_FORMATTER),
                booking.getEndTime().format(TIME_FORMATTER),
                booking.getTherapist() != null ? booking.getTherapist().getFullName() : "Chưa chọn",
                booking.getFinalPrice().longValue(),
                booking.getBookingId()
        );
    }

    /**
     * Booking cancellation email
     */
    public String bookingCancellation(Booking booking) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #f44336; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background: #f9f9f9; }
                    .booking-info { background: white; padding: 15px; margin: 15px 0; border-left: 4px solid #f44336; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Xác Nhận Hủy Lịch</h1>
                    </div>
                    
                    <div class="content">
                        <p>Xin chào <strong>%s</strong>,</p>
                        
                        <p>Lịch hẹn của bạn đã được hủy thành công.</p>
                        
                        <div class="booking-info">
                            <div><strong>Mã đặt lịch:</strong> %s</div>
                            <div><strong>Dịch vụ:</strong> %s</div>
                            <div><strong>Ngày:</strong> %s</div>
                            <div><strong>Giờ:</strong> %s</div>
                        </div>
                        
                        <p>Bạn có thể đặt lịch mới bất kỳ lúc nào trên website của chúng tôi.</p>
                        
                        <p>Trân trọng,<br>Spa Team</p>
                    </div>
                </div>
            </body>
            </html>
            """,
                booking.getCustomerName(),
                booking.getBookingId(),
                booking.getService().getName(),
                booking.getBookingDate().format(DATE_FORMATTER),
                booking.getBookingTime().format(TIME_FORMATTER)
        );
    }

    /**
     * Booking reminder (24h before)
     */
    public String bookingReminder(Booking booking) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #FF9800; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background: #f9f9f9; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔔 Nhắc Nhở Lịch Hẹn</h1>
                    </div>
                    
                    <div class="content">
                        <p>Xin chào <strong>%s</strong>,</p>
                        
                        <p>Đây là lời nhắc về lịch hẹn của bạn vào <strong>ngày mai</strong>:</p>
                        
                        <div style="background: white; padding: 15px; margin: 15px 0;">
                            <div><strong>Dịch vụ:</strong> %s</div>
                            <div><strong>Thời gian:</strong> %s, %s</div>
                            <div><strong>Địa điểm:</strong> 123 Đường ABC, Quận 1, TP.HCM</div>
                        </div>
                        
                        <p>Hẹn gặp bạn!</p>
                    </div>
                </div>
            </body>
            </html>
            """,
                booking.getCustomerName(),
                booking.getService().getName(),
                booking.getBookingDate().format(DATE_FORMATTER),
                booking.getBookingTime().format(TIME_FORMATTER)
        );
    }
}