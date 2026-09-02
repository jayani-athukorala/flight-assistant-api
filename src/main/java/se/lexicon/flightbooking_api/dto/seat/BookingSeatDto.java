package se.lexicon.flightbooking_api.dto.seat;

import se.lexicon.flightbooking_api.entity.enums.SeatClass;

import java.math.BigDecimal;

public record BookingSeatDto(
        Long id,
        Long passengerId,
        Long flightId,
        Long flightSeatId,
        String seatNumber,
        SeatClass seatClass,
        BigDecimal price
) {}
