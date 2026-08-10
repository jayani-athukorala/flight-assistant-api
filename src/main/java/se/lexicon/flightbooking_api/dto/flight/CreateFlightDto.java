package se.lexicon.flightbooking_api.dto.flight;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;


public record CreateFlightDto(

        @NotBlank
        String flightNumber,


        @NotBlank
        String airline,


        @NotBlank
        String origin,


        @NotBlank
        String destination,


        @NotNull
        LocalDateTime departureTime,


        @NotNull
        LocalDateTime arrivalTime

) {

}