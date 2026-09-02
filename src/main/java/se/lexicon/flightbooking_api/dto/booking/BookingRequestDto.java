package se.lexicon.flightbooking_api.dto.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import se.lexicon.flightbooking_api.dto.passenger.PassengerRequestDto;

import java.util.List;

public record BookingRequestDto(

        @NotNull
        Long outboundFlightId,

        Long returnFlightId,

        @Valid
        @NotEmpty
        List<PassengerRequestDto> passengers

) {}