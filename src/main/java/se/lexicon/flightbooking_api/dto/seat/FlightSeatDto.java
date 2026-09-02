package se.lexicon.flightbooking_api.dto.seat;

import se.lexicon.flightbooking_api.entity.enums.SeatClass;

import java.math.BigDecimal;

public record FlightSeatDto(
        Long id,
        String seatNumber,
        SeatClass seatClass,
        BigDecimal price,
        boolean available
) {}