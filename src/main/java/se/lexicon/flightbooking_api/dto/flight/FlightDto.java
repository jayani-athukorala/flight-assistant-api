package se.lexicon.flightbooking_api.dto.flight;

import se.lexicon.flightbooking_api.dto.airport.AirportResponseDto;
import se.lexicon.flightbooking_api.entity.enums.FlightStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FlightDto(
        Long id,
        String flightNumber,
        String airline,
        String createdByEmail,
        AirportResponseDto origin,
        AirportResponseDto destination,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        FlightStatus status,
        BigDecimal startingPrice
) {}
