package com.trongtin.spabooking.mapper;

import com.trongtin.spabooking.dto.response.BookingResponse;
import com.trongtin.spabooking.dto.response.ServiceDTO;
import com.trongtin.spabooking.dto.response.TherapistDTO;
import com.trongtin.spabooking.dto.response.UserDTO;
import com.trongtin.spabooking.entity.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking) {
        BookingResponse response = BookingResponse.builder()
                .id(booking.getId())
                .bookingId(booking.getBookingId())
                .customerName(booking.getCustomerName())
                .customerPhone(booking.getCustomerPhone())
                .customerEmail(booking.getCustomerEmail())
                .bookingDate(booking.getBookingDate())
                .bookingTime(booking.getBookingTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus().name())
                .isAnonymous(booking.getIsAnonymous())
                .originalPrice(booking.getOriginalPrice())
                .discountAmount(booking.getDiscountAmount())
                .finalPrice(booking.getFinalPrice())
                .paymentStatus(booking.getPaymentStatus().name())
                .customerNote(booking.getCustomerNote())
                .adminNote(booking.getAdminNote())
                .createdAt(booking.getCreatedAt())
                .build();

        // Map service
        if (booking.getService() != null) {
            response.setService(toServiceDTO(booking.getService()));
        }

        // Map therapist
        if (booking.getTherapist() != null) {
            response.setTherapist(toTherapistDTO(booking.getTherapist()));
        }

        // Set payment method if exists
        if (booking.getPaymentMethod() != null) {
            response.setPaymentMethod(booking.getPaymentMethod().name());
        }

        // Calculate permissions
        response.setCanCancel(canCancelBooking(booking));
        response.setCanReview(canReviewBooking(booking));

        return response;
    }

    public ServiceDTO toServiceDTO(Service service) {
        return ServiceDTO.builder()
                .id(service.getId())
                .name(service.getName())
                .slug(service.getSlug())
                .description(service.getDescription())
                .durationMinutes(service.getDurationMinutes())
                .price(service.getPrice())
                .discountPrice(service.getDiscountPrice())
                .imageUrl(service.getImageUrl())
                .category(service.getCategory())
                .displayOrder(service.getDisplayOrder())
                .build();
    }

    public TherapistDTO toTherapistDTO(Therapist therapist) {
        return TherapistDTO.builder()
                .id(therapist.getId())
                .fullName(therapist.getFullName())
                .phone(therapist.getPhone())
                .email(therapist.getEmail())
                .employeeCode(therapist.getEmployeeCode())
                .avatarUrl(therapist.getAvatarUrl())
                .build();
    }

    public TherapistDTO toTherapistDTO(TherapistService ts) {
        TherapistDTO dto = toTherapistDTO(ts.getTherapist());
        dto.setSkillLevel(ts.getSkillLevel().name());
        dto.setYearsExperience(ts.getYearsExperience());
        dto.setIsExpert(ts.getSkillLevel() == SkillLevel.EXPERT);
        return dto;
    }

    public UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .avatarUrl(user.getAvatarUrl())
                .loyaltyPoints(user.getLoyaltyPoints())
                .membershipTier(user.getMembershipTier().name())
                .isVerified(user.getIsVerified())
                .build();
    }

    private boolean canCancelBooking(Booking booking) {
        // Can cancel if:
        // 1. Status is PENDING or CONFIRMED
        // 2. At least 2 hours before booking time

        if (booking.getStatus() != BookingStatus.PENDING &&
                booking.getStatus() != BookingStatus.CONFIRMED) {
            return false;
        }

        LocalDateTime bookingDateTime = LocalDateTime.of(
                booking.getBookingDate(),
                booking.getBookingTime()
        );

        long hoursUntil = Duration.between(LocalDateTime.now(), bookingDateTime).toHours();

        return hoursUntil >= 2;
    }

    private boolean canReviewBooking(Booking booking) {
        // Can review if status is COMPLETED
        return booking.getStatus() == BookingStatus.COMPLETED;
    }
}