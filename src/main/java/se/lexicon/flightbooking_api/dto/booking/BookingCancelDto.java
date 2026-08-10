package se.lexicon.flightbooking_api.dto.booking;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record BookingCancelDto(

        @NotBlank
        Long flightId,

        @Email
        @NotBlank
        String email

) {

}