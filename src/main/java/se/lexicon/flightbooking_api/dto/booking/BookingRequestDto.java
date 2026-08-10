package se.lexicon.flightbooking_api.dto.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import se.lexicon.flightbooking_api.dto.passenger.PassengerDto;
import se.lexicon.flightbooking_api.entity.enums.SeatClass;
import se.lexicon.flightbooking_api.entity.enums.TripType;


import java.util.List;

public record BookingRequestDto(

        @NotNull
        Long outboundFlightId,

        Long returnFlightId,

        @NotNull
        TripType tripType,

        @NotNull
        SeatClass seatClass,

        @Valid
        @NotEmpty
        List<PassengerDto> passengers
) {

}