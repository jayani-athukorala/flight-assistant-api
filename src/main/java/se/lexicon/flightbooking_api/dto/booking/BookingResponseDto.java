package se.lexicon.flightbooking_api.dto.booking;

import se.lexicon.flightbooking_api.dto.flight.FlightDto;
import se.lexicon.flightbooking_api.dto.passenger.PassengerResponseDto;
import se.lexicon.flightbooking_api.dto.seat.BookingSeatDto;
import se.lexicon.flightbooking_api.entity.enums.BookingStatus;
import se.lexicon.flightbooking_api.entity.enums.TripType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingResponseDto(
        Long id,
        String bookingReference,
        LocalDateTime bookingDate,
        LocalDateTime cancelledAt,
        LocalDateTime archivedAt,
        BookingStatus status,
        TripType tripType,
        FlightDto outboundFlight,
        FlightDto returnFlight,
        BigDecimal totalPrice,
        List<PassengerResponseDto> passengers,
        List<BookingSeatDto> seats
) {}