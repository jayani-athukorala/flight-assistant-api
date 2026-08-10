package se.lexicon.flightbooking_api.dto.seat;

import se.lexicon.flightbooking_api.entity.enums.SeatClass;

public record BookedSeatDto(

        Long id,
        String seatNumber,
        SeatClass seatClass,
        Double price

) {}
