package se.lexicon.flightbooking_api.dto.flight;

import se.lexicon.flightbooking_api.entity.enums.FlightStatus;

import java.time.LocalDateTime;

public record FlightDto(

        Long id,

        String flightNumber,

        String airline,

        String origin,

        String destination,

        LocalDateTime departureTime,

        LocalDateTime arrivalTime,

        FlightStatus status,

        Double startingPrice

) {

}
