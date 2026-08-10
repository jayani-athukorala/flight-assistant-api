package se.lexicon.flightbooking_api.dto.booking;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public record BookingLookupDto(

        @Email
        @NotBlank
        String email

) {

}