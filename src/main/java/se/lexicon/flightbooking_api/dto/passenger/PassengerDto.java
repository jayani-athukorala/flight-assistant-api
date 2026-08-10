package se.lexicon.flightbooking_api.dto.passenger;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public record PassengerDto(

        @NotBlank
        String firstName,


        @NotBlank
        String lastName,


        @NotBlank
        String passportNumber,


        @Email
        @NotBlank
        String email

) {

}
