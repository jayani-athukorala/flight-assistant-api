package se.lexicon.flightbooking_api.dto.auth;

public record LoginResponseDto(

        String token,

        String email

) {
}
