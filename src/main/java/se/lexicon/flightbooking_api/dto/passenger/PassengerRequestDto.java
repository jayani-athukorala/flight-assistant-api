package se.lexicon.flightbooking_api.dto.passenger;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PassengerRequestDto(

        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        @NotBlank
        String passportNumber,

        @Email
        @NotBlank
        String email,

        @NotNull
        Long outboundSeatId,

        Long returnSeatId

) {}