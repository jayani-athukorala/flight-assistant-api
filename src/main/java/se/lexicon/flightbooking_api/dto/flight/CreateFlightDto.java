package se.lexicon.flightbooking_api.dto.flight;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record CreateFlightDto(

        @NotBlank(message = "Flight number is required")
        String flightNumber,

        @NotBlank(message = "Airline is required")
        String airline,

        @NotNull(message = "Origin airport is required")
        @Positive(message = "Origin airport ID must be positive")
        Long originAirportId,

        @NotNull(message = "Destination airport is required")
        @Positive(message = "Destination airport ID must be positive")
        Long destinationAirportId,

        @NotNull(message = "Departure time is required")
        @Future(message = "Departure time must be in the future")
        LocalDateTime departureTime,

        @NotNull(message = "Arrival time is required")
        @Future(message = "Arrival time must be in the future")
        LocalDateTime arrivalTime

) {}