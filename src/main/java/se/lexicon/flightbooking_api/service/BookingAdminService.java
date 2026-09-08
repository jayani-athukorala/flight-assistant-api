package se.lexicon.flightbooking_api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import se.lexicon.flightbooking_api.dto.booking.BookingResponseDto;
import se.lexicon.flightbooking_api.entity.enums.BookingStatus;

import java.time.LocalDate;

public interface BookingAdminService {

    Page<BookingResponseDto> searchBookings(
            BookingStatus status,
            String createdByEmail,
            String bookingReference,
            String passportNumber,
            Long flightId,
            LocalDate from,
            LocalDate to,
            Boolean archived,
            Pageable pageable
    );
}
